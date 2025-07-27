package RPC;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class PaxosKeyValueStoreServer implements KeyValueStore {
    public final int serverId;
    private final Map<String, String> store = new ConcurrentHashMap<>();
    private final Map<String, Integer> highestPromised = new ConcurrentHashMap<>();

    private List<KeyValueStore> replicas = new ArrayList<>();

    private volatile boolean isAlive = true;
    private final Random random = new Random();

    public PaxosKeyValueStoreServer(int serverId) {
        this.serverId = serverId;
        startRecoveryThread();
    }

    public void setReplicas(List<KeyValueStore> replicas) {
        this.replicas = replicas;
    }

    // Client-side operations (Proposer)
    @Override
    public String put(String key, String value) throws RemoteException {
        logMessage("Initiating PAXOS for PUT - Key: " + key + ", Value: " + value);
        return propose(key, value, false);
    }

    @Override
    public String delete(String key) throws RemoteException {
        logMessage("Initiating PAXOS for DELETE - Key: " + key);
        return propose(key, null, true);
    }

    @Override
    public String get(String key) throws RemoteException {
        logMessage("GET request received for Key: " + key);
        if (store.containsKey(key)) {
            logMessage("GET OK - Key: " + key + " VALUE: " + store.get(key));
            return "VALUE: " + store.get(key);
        } else {
            logMessage("ERROR: Key not found");
            return "ERROR: Key not found";
        }
    }

    private String propose(String key, String value, boolean isDelete) throws RemoteException {
        int proposalNumber = generateProposalNumber();

        // Phase 1: Prepare
        int promises = 0;
        for (KeyValueStore replica : replicas) {
            PrepareResponse response = replica.onPrepare(new PrepareRequest(proposalNumber, key));
            if (response.promised) {
                promises++;
            }
        }

        if (promises <= replicas.size() / 2) {
            logMessage("ERROR: Paxos Prepare phase failed due to majority not reached. Aborting..");
            throw new RemoteException("ERROR: Paxos Prepare phase failed.");
        }

        // Phase 2: Accept
        int accepts = 0;
        for (KeyValueStore replica : replicas) {
            AcceptResponse response = replica.onAccept(new AcceptRequest(proposalNumber, key));
            if (response.accepted) {
                accepts++;
            }
        }

        if (accepts <= replicas.size() / 2) {
            logMessage("ERROR: Paxos Accept phase failed due to majority not reached. Aborting..");
            throw new RemoteException("ERROR: Paxos Accept phase failed.");
        }

        // Phase 3: Learn
        onLearn(key, value, isDelete);
        for (KeyValueStore replica : replicas) {
            try {
                replica.onLearn(key, value, isDelete);
            } catch (Exception ignored) {}
        }

        if (isDelete) {
            logMessage("DELETE OK: " + key);
        } else {
            logMessage("PUT OK: " + key);
        }
        return isDelete ? "DELETE OK: " + key : "PUT OK: " + key;
    }

    private int generateProposalNumber() {
        return (int) System.currentTimeMillis();
    }

    private boolean simulateFailure() {
        if (!isAlive) {
            return true;
        }

        if (random.nextInt(100) < 10) {
            isAlive = false;
            logMessage("[FAILURE] Acceptor " + serverId + " has randomly failed.");
            return true;
        }

        return false;
    }

    private void startRecoveryThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                    if (!isAlive) {
                        isAlive = true;
                        logMessage("[RECOVERY] Acceptor " + serverId + " has recovered.");
                    }
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    // Paxos RPC Methods (Acceptor)
    @Override
    public PrepareResponse onPrepare(PrepareRequest request) throws RemoteException{
        if (simulateFailure()) {
            return new PrepareResponse(false);
        }

        int highest = highestPromised.getOrDefault(request.key, -1);
        boolean promised = request.proposalNumber > highest;
        if (promised) {
            highestPromised.put(request.key, request.proposalNumber);
        }
        return new PrepareResponse(promised);
    }

    @Override
    public AcceptResponse onAccept(AcceptRequest request){
        if (simulateFailure()) {
            return new AcceptResponse(false);
        }

        int highest = highestPromised.getOrDefault(request.key, -1);
        boolean accepted = request.proposalNumber >= highest;
        return new AcceptResponse(accepted);
    }

    // Paxos RPC Method (Learner)
    @Override
    public void onLearn(String key, String value, boolean isDelete) throws RemoteException {
        if (isDelete) {
            store.remove(key);
        } else {
            store.put(key, value);
        }
    }

    public void logMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        System.out.println("[LOG][Server " + this.serverId + "] " + timestamp + " - " + message);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java RPC.PaxosKeyValueStoreServer <serverId>");
            return;
        }

        int serverId = Integer.parseInt(args[0]);
        try {
            // Start this server
            PaxosKeyValueStoreServer server = new PaxosKeyValueStoreServer(serverId);
            KeyValueStore stub = (KeyValueStore) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(1099 + serverId);
            registry.rebind("KeyValueStore", stub);
            server.logMessage("Paxos Key-Value Store Server is running...");

            // Give time for all servers to start
            Thread.sleep(5000);

            // Connect to replicas
            List<KeyValueStore> replicas = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                if (i != serverId) { // skip self
                    try {
                        Registry replicaRegistry = LocateRegistry.getRegistry(1099 + i);
                        KeyValueStore replica = (KeyValueStore) replicaRegistry.lookup("KeyValueStore");
                        replicas.add(replica);
                        server.logMessage("Connected to replica server " + i);
                    } catch (Exception e) {
                        server.logMessage("Failed to connect to replica " + i + ": " + e.getMessage());
                    }
                }
            }

            // Set the replicas into the server instance
            server.setReplicas(replicas);

        } catch (Exception e) {
            System.out.println("Failed to start server " + serverId);
        }
    }

}
