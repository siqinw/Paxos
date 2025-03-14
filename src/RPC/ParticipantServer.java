package RPC;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class ParticipantServer implements Participant {
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private final int serverId;

    public ParticipantServer(int serverId) {
        this.serverId = serverId;
    }

    public String get(String key) throws RemoteException {
        logMessage("GET request - Key: " + key);
        return store.containsKey(key) ? "VALUE: " + store.get(key) : "ERROR: Key not found";
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
