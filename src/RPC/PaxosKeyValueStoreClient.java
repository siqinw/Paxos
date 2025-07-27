package RPC;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.*;

public class PaxosKeyValueStoreClient {

    private final List<KeyValueStore> servers = new ArrayList<>();

    public PaxosKeyValueStoreClient() {
        try {
            // Connect to all 5 servers
            for (int i = 1; i <= 5; i++) {
                Registry registry = LocateRegistry.getRegistry(1099 + i);
                KeyValueStore server = (KeyValueStore) registry.lookup("KeyValueStore");
                servers.add(server);
                logMessage("Connected to server " + i);
            }
        } catch (Exception e) {
            logMessage("Client connection error: " + e.getMessage());
        }
    }

    private KeyValueStore getRandomServer() {
        Random random = new Random();
        return servers.get(random.nextInt(servers.size()));
    }

    private String tryOperation(Operation operation) {
        int attempts = 10;
        while (attempts-- > 0) {
            try {
                return operation.execute();
            } catch (Exception e) {
                logMessage("Operation failed. Retrying...");
                try {
                    Thread.sleep(2000); // wait a bit before retry
                } catch (InterruptedException ignored) {}
            }
        }
        return "ERROR: Operation failed after retries.";
    }

    @FunctionalInterface
    interface Operation {
        String execute() throws Exception;
    }

    public void prepopulateStore() {
        System.out.println("Prepopulating store...");
        for (int i = 1; i <= 5; i++) {
            try {
                int finalI = i;
                String response = tryOperation(() -> getRandomServer().put("key" + finalI, "value" + finalI));
                logMessage("PUT key" + i + ": " + response);
            } catch (Exception e) {
                logMessage("Error prepopulating: " + e.getMessage());
            }
        }
    }

    public void performOperations() {
        System.out.println("Performing 5 PUTs, 5 GETs, 5 DELETEs...");
        // 5 PUTs
        for (int i = 6; i <= 10; i++) {
            try {
                int finalI = i;
                String response = tryOperation(() -> getRandomServer().put("key" + finalI, "value" + finalI));
                logMessage("PUT key" + i + ": " + response);
            } catch (Exception e) {
                logMessage("Error during PUT: " + e.getMessage());
            }
        }

        // 5 GETs
        for (int i = 1; i <= 5; i++) {
            try {
                int finalI = i;
                String response = tryOperation(() -> getRandomServer().get("key" + finalI));
                logMessage("GET key" + i + ": " + response);
            } catch (Exception e) {
                logMessage("Error during GET: " + e.getMessage());
            }
        }

        // 5 DELETEs
        for (int i = 1; i <= 5; i++) {
            try {
                int finalI = i;
                String response = tryOperation(() -> getRandomServer().delete("key" + finalI));
                logMessage("DELETE key" + i + ": " + response);
            } catch (Exception e) {
                logMessage("Error during DELETE: " + e.getMessage());
            }
        }
    }

    private static void logMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        System.out.println("[LOG] " + timestamp + " - " + message);
    }

    public void interactiveSession() {
        Scanner scanner = new Scanner(System.in);
        logMessage("Client ready. Enter commands (PUT <key> <value>, GET <key>, DELETE <key>, exit):");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            String[] parts = line.trim().split(" ", 3);

            if (parts.length == 0) continue;
            String command = parts[0].toUpperCase();

            try {
                switch (command) {
                    case "PUT":
                        if (parts.length < 3) {
                            logMessage("Usage: PUT <key> <value>");
                        } else {
                            logMessage("Sending PUT request: key=" + parts[1] + ", value=" + parts[2]);
                            String response = tryOperation(() -> getRandomServer().put(parts[1], parts[2]));
                            logMessage("Response: " + response);
                        }
                        break;
                    case "GET":
                        if (parts.length < 2) {
                            logMessage("Usage: GET <key>");
                        } else {
                            logMessage("Sending GET request: key=" + parts[1]);
                            String response = tryOperation(() -> getRandomServer().get(parts[1]));
                            logMessage("Response: " + response);
                        }
                        break;
                    case "DELETE":
                        if (parts.length < 2) {
                            logMessage("Usage: DELETE <key>");
                        } else {
                            logMessage("Sending DELETE request: key=" + parts[1]);
                            String response = tryOperation(() -> getRandomServer().delete(parts[1]));
                            logMessage("Response: " + response);
                        }
                        break;
                    case "EXIT":
                        logMessage("Exiting client.");
                        scanner.close();
                        return;
                    default:
                        logMessage("Unknown command. Available commands: PUT, GET, DELETE, EXIT.");
                        break;
                }
            } catch (Exception e) {
                logMessage("Error during operation: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        PaxosKeyValueStoreClient client = new PaxosKeyValueStoreClient();
        client.prepopulateStore();
        client.performOperations();
        client.interactiveSession();
    }
}
