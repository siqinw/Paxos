package RPC;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CoordinatorServer implements KeyValueStore {
    private static final List<KeyValueStore> replicas = new ArrayList<>();

    @Override
    public String put(String key, String value) throws RemoteException {
        logMessage("Initiating 2PC for PUT - Key: " + key + ", Value: " + value);

        for (KeyValueStore replica : replicas) {
            if (!replica.prepare(key, value, false)) {
                logMessage("Prepare phase failed. Aborting transaction.");
                abortTransaction(key);
                return "ERROR: Transaction aborted.";
            }
        }

        for (KeyValueStore replica : replicas) {
            replica.commit(key, value, false);
        }
        return "PUT OK: " + key;
    }

    @Override
    public String get(String key) throws RemoteException {
        logMessage("GET request received for Key: " + key);
        int index = new Random().nextInt(replicas.size());
        return replicas.get(index).get(key);
    }

    @Override
    public String delete(String key) throws RemoteException {
        logMessage("Initiating 2PC for DELETE - Key: " + key);

        for (KeyValueStore replica : replicas) {
            if (!replica.prepare(key, null, true)) {
                logMessage("Prepare phase failed. Aborting transaction.");
                abortTransaction(key);
                return "ERROR: Transaction aborted.";
            }
        }

        for (KeyValueStore replica : replicas) {
            replica.commit(key, null, true);
        }

        return "DELETE OK: " + key;
    }

    @Override
    public boolean prepare(String key, String value, boolean isDelete) throws RemoteException {
        return true;
    }

    @Override
    public boolean commit(String key, String value, boolean isDelete) throws RemoteException {
        return true;
    }

    @Override
    public void abort(String key) throws RemoteException {
    }

    private void abortTransaction(String key) throws RemoteException {
        for (KeyValueStore replica : replicas) {
            replica.abort(key);
        }
    }

    private static void logMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        System.out.println("[LOG][CoordinatorServer] " + timestamp + " - " + message);
    }

    public static void main(String[] args) {
        if (args.length >= 1) {
            System.out.println("Usage: java RPC.CoordinatorServer");
            return;
        }

        try {
            CoordinatorServer coordinator = new CoordinatorServer();
            KeyValueStore stub = (KeyValueStore) UnicastRemoteObject.exportObject(coordinator, 0);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("CoordinatorServer", stub);
            logMessage("Coordinator Server is running...");
        } catch (RemoteException e) {
            logMessage("Coordinator Server failed to start...");
        }

        // Launch 5 replicas
        for (int i=1; i<=5; i++) {
            try {
                ParticipantServer server = new ParticipantServer(i);
                KeyValueStore stub = (KeyValueStore) UnicastRemoteObject.exportObject(server, 0);
                Registry registry = LocateRegistry.createRegistry(1099+i);
                registry.rebind("ParticipantServer" + i, stub);
                logMessage("Participant Server " + i + " is running...");
            } catch (RemoteException e) {
                logMessage("Participant Server " + i + " failed to start...");
            }
        }

        // Connect 5 replicas
        for (int i=1; i<=5; i++) {
            try {
                Registry replicaRegistry = LocateRegistry.getRegistry(1099 + i);
                KeyValueStore replica = (KeyValueStore) replicaRegistry.lookup("ParticipantServer" + i);
                replicas.add(replica);
                logMessage("Connected to replica " + i);
            } catch (Exception e) {
                logMessage("Failed to connect to replica " + i);
            }
        }

    }

}
