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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CoordinatorServer implements Coordinator {
    private static final List<Participant> replicas = new ArrayList<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public String put(String key, String value) throws RemoteException {
        try {
            return executorService.submit(() -> {
                logMessage("Initiating 2PC for PUT - Key: " + key + ", Value: " + value);

                for (Participant replica : replicas) {
                    if (!replica.prepare(key, value, false)) {
                        logMessage("Prepare phase failed. Aborting transaction.");
                        abortTransaction(key);
                        return "ERROR: Transaction aborted.";
                    }
                }

                for (Participant replica : replicas) {
                    replica.commit(key, value, false);
                }
                return "PUT OK: " + key;
            }).get(2, TimeUnit.SECONDS);
        } catch (TimeoutException t) {
            logMessage("Timeout");
            abortTransaction(key);
            return "ERROR: timeout identified";
        } catch (Exception e) {
            logMessage("ERROR: " + e.getMessage());
            return "ERROR: Operation failed";
        }
    }

    public String get(String key) throws RemoteException {
        try {
            return executorService.submit(() -> {
                logMessage("GET request received for Key: " + key);
                int index = new Random().nextInt(replicas.size());
                return replicas.get(index).get(key);
            }).get(2, TimeUnit.SECONDS);
        } catch (TimeoutException t) {
            logMessage("Timeout");
            abortTransaction(key);
            return "ERROR: timeout identified";
        } catch (Exception e) {
            logMessage("ERROR: " + e.getMessage());
            return "ERROR: Operation failed";
        }
    }

    public String delete(String key) throws RemoteException {
        try {
            return executorService.submit(() -> {
                logMessage("Initiating 2PC for DELETE - Key: " + key);
                for (Participant replica : replicas) {
                    if (!replica.prepare(key, null, true)) {
                        logMessage("Prepare phase failed. Aborting transaction.");
                        abortTransaction(key);
                        return "ERROR: Transaction aborted.";
                    }
                }
                for (Participant replica : replicas) {
                    replica.commit(key, null, true);
                }
                return "DELETE OK: " + key;
            }).get(2, TimeUnit.SECONDS);
        } catch (TimeoutException t) {
            logMessage("Timeout");
            abortTransaction(key);
            return "ERROR: timeout identified";
        } catch (Exception e) {
            logMessage("ERROR: " + e.getMessage());
            return "ERROR: Operation failed";
        }

    }

    private void abortTransaction(String key) throws RemoteException {
        for (Participant replica : replicas) {
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
            Coordinator coordinator = new CoordinatorServer();
            Coordinator stub = (Coordinator) UnicastRemoteObject.exportObject(coordinator, 0);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("CoordinatorServer", stub);
            logMessage("Coordinator Server is running...");
        } catch (RemoteException e) {
            logMessage("Coordinator Server failed to start...");
        }

        // Launch 5 replicas
        for (int i=1; i<=5; i++) {
            try {
                Participant server = new ParticipantServer(i);
                Participant stub = (Participant) UnicastRemoteObject.exportObject(server, 0);
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
                Participant replica = (Participant) replicaRegistry.lookup("ParticipantServer" + i);
                replicas.add(replica);
                logMessage("Connected to replica " + i);
            } catch (Exception e) {
                logMessage("Failed to connect to replica " + i);
            }
        }

    }

}
