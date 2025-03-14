package RPC;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Participant extends Remote {
    // 2PC Methods
    boolean prepare(String key, String value, boolean isDelete) throws RemoteException;

    boolean commit(String key, String value, boolean isDelete) throws RemoteException;

    void abort(String key) throws RemoteException;

    String get(String key) throws RemoteException;
}
