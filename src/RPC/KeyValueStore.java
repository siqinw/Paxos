package RPC;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Define the Remote Interface for the Key-Value Store
public interface KeyValueStore extends Remote {

    // Method to put a key-value pair into the store
    String put(String key, String value) throws RemoteException;

    // Method to get a value by key
    String get(String key) throws RemoteException;

    // Method to delete a key from the store
    String delete(String key) throws RemoteException;

    // 2PC Methods
    boolean prepare(String key, String value, boolean isDelete) throws RemoteException;
    boolean commit(String key, String value, boolean isDelete) throws RemoteException;
    void abort(String key) throws RemoteException;
}

