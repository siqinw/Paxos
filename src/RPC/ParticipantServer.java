package RPC;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParticipantServer implements KeyValueStore {
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private int serverId;

    public ParticipantServer(int serverId) {
        this.serverId = serverId;
    }

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

    public boolean prepare(String key, String value, boolean isDelete) throws RemoteException {
        logMessage("Prepare request received for Key: " + key);
        return true;
    }

    public boolean commit(String key, String value, boolean isDelete) throws RemoteException {
        logMessage("Commit request received for Key: " + key);
        if (isDelete) {
            store.remove(key);
        } else {
            store.put(key, value);
        }
        return true;
    }

    public void abort(String key) throws RemoteException {
        logMessage("Abort request received for Key: " + key);
    }

    private void logMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        System.out.println("[LOG][ParticipantServer " + serverId + "] " + timestamp + " - " + message);
    }

}
