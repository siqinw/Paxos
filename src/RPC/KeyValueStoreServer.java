package RPC;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;

// Implement the Key-Value Store as a Single-threaded RMI Server
public class KeyValueStoreServer implements KeyValueStore {
    private final HashMap<String, String> store = new HashMap<>();

    @Override
    public synchronized String put(String key, String value) throws RemoteException {
        logMessage("PUT request - Key: " + key + ", Value: " + value);
        store.put(key, value);
        return "PUT OK: " + key;
    }

    @Override
    public synchronized String get(String key) throws RemoteException {
        logMessage("GET request - Key: " + key);
        return store.containsKey(key) ? "VALUE: " + store.get(key) : "ERROR: Key not found";
    }

    @Override
    public synchronized String delete(String key) throws RemoteException {
        logMessage("DELETE request - Key: " + key);
        return store.remove(key) != null ? "DELETE OK: " + key : "ERROR: Key not found";
    }

    private static void logMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        System.out.println("[LOG] " + timestamp + " - " + message);
    }

    public static void main(String[] args) {
        try {
            KeyValueStoreServer server = new KeyValueStoreServer();
            KeyValueStore stub = (KeyValueStore) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("KeyValueStore", stub);
            logMessage("Single-threaded Key-Value Store RMI Server is running...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
