package RPC;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KeyValueStoreServer implements KeyValueStore {
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public String get(String key) throws RemoteException {
        try {
            return executorService.submit(() -> {
                logMessage("GET request - Key: " + key);
                return store.containsKey(key) ? "VALUE: " + store.get(key) : "ERROR: Key not found";
            }).get();
        } catch (Exception e) {
            logMessage("ERROR: " + e.getMessage());
            return "ERROR: Operation failed";
        }
    }

    public String put(String key, String value) throws RemoteException {
        try {
            return executorService.submit(() -> {
                store.put(key, value);
                logMessage("PUT request - Key: " + key + ", Value: " + value);
                return "PUT OK: " + key;
            }).get();
        } catch (Exception e) {
            logMessage("ERROR: " + e.getMessage());
            return "ERROR: Operation failed";
        }
    }

    @Override
    public String delete(String key) throws RemoteException {
        try {
            return executorService.submit(() -> {
                logMessage("DELETE request - Key: " + key);
                return store.remove(key) != null ? "DELETE OK: " + key : "ERROR: Key not found";
            }).get();
        } catch (Exception e) {
            logMessage("ERROR: " + e.getMessage());
            return "ERROR: Operation failed";
        }
    }

    private static void logMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        System.out.println("[LOG] " + timestamp + " - " + message);
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logMessage("Shutting down server...");
        }));
        try {
            KeyValueStoreServer server = new KeyValueStoreServer();
            KeyValueStore stub = (KeyValueStore) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("KeyValueStore", stub);
            logMessage("Key-Value Store RMI Server is running and ready to accept client connections...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
