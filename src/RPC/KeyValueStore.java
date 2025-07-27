package RPC;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KeyValueStore extends Remote {
    // Client-facing operations
    String put(String key, String value) throws RemoteException;
    String get(String key) throws RemoteException;
    String delete(String key) throws RemoteException;

    // Paxos RPC methods
    PrepareResponse onPrepare(PrepareRequest request) throws RemoteException;
    AcceptResponse onAccept(AcceptRequest request) throws RemoteException;
    void onLearn(String key, String value, boolean isDelete) throws RemoteException;
}
