package RPC;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Coordinator extends Remote {
    // Method to put a key-value pair into the store
    String put(String key, String value) throws RemoteException;

    // Method to get a value by key
    String get(String key) throws RemoteException;

    // Method to delete a key from the store
    String delete(String key) throws RemoteException;
}
