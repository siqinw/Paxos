package RPC;

import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class KeyValueStoreClient {
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
            KeyValueStore store = (KeyValueStore) registry.lookup("KeyValueStore");

            Scanner scanner = new Scanner(System.in);
            logMessage("Connected to Key-Value Store RMI Server.");
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
