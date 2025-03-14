package RPC;

import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client {
    private static void logMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        System.out.println("[LOG] " + timestamp + " - " + message);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java KeyValueStoreClient <server_address>");
            return;
        }

        String serverAddress = args[0];

        try {
            // Locate the RMI registry
            Registry registry = LocateRegistry.getRegistry(serverAddress, 1099);
            KeyValueStore store = (KeyValueStore) registry.lookup("CoordinatorServer");

            Scanner scanner = new Scanner(System.in);
            logMessage("Connected to Key-Value Store RMI Server.");

            // Pre-populating the key-value store
            logMessage("Pre-populating Key-Value Store with initial data...");
            String[] initialData =  {"key1 value1", "key2 value2", "key3 value3", "key4 value4", "key5 value5"};
            for (String data : initialData) {
                String[] parts = data.split(" ", 2);
                String key = parts[0];
                String value = parts[1];
                try {
                    String response = store.put(key, value);
                    logMessage("Command: PUT " + data + " - Response: " + response);
                } catch (Exception e) {
                    logMessage("ERROR during pre-population: " + e.getMessage());
                }
            }

            logMessage("Executing 5 PUTs, 5 GETs, 5 DELETEs...");
            String[] commands = {
                    "PUT key6 value6", "PUT key7 value7", "PUT key8 value8", "PUT key9 value9", "PUT key10 value10",
                    "GET key1", "GET key2", "GET key3", "GET key4", "GET key5",
                    "DELETE key1", "DELETE key2", "DELETE key3", "DELETE key4", "DELETE key5"
            };

            for (String cmd : commands) {
                String[] parts = cmd.split(" ", 3);
                String operation = parts[0];
                String key = parts[1];
                String response = "";
                try {
                    switch (operation) {
                        case "PUT":
                            response = store.put(key, parts[2]);
                            break;
                        case "GET":
                            response = store.get(key);
                            break;
                        case "DELETE":
                            response = store.delete(key);
                            break;
                    }
                    logMessage("Command: " + cmd + " - Response: " + response);
                } catch (Exception e) {
                    logMessage("ERROR during initialization: " + e.getMessage());
                }
            }

            System.out.println("Enter commands (PUT <key> <value>, GET <key>, DELETE <key>) or 'exit' to quit.");
            while (true) {
                logMessage("Waiting for user input");
                System.out.print("Command: ");
                String command = scanner.nextLine().trim();
                if (command.equalsIgnoreCase("exit")) break;

                String[] parts = command.split(" ", 3);
                if (parts.length < 2) {
                    System.out.println("ERROR: Invalid command format.");
                    continue;
                }

                String operation = parts[0].toUpperCase();
                String key = parts[1];
                String response = "";

                try {
                    switch (operation) {
                        case "PUT":
                            if (parts.length < 3) {
                                System.out.println("ERROR: PUT requires a key and value.");
                                continue;
                            }
                            response = store.put(key, parts[2]);
                            break;
                        case "GET":
                            response = store.get(key);
                            break;
                        case "DELETE":
                            response = store.delete(key);
                            break;
                        default:
                            System.out.println("ERROR: Unknown command.");
                            continue;
                    }
                } catch (ConnectException e) {
                    logMessage("ERROR: Server connection lost. Exiting...");
                    break;
                }

                logMessage("Response: " + response);
            }

            scanner.close();
        } catch (Exception e) {
            logMessage("ERROR: Failed to connect to server. Exiting...");
            return;
        }
    }
}
