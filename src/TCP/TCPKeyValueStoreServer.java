package TCP;
import java.io.*;
import java.net.*;
import java.util.HashMap;

public class TCPKeyValueStoreServer {
    private static final HashMap<String, String> store = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java TCPKeyValueStoreServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        startTCPServer(port);
    }

    public static void startTCPServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logMessage("TCP Server started and listening on port: " + port);

            while (true) {
                try (  /* Wait for a client to connect and accept the connection */
                        Socket clientSocket = serverSocket.accept();
                        /* Set up a reader to receive messages from the client */
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        /* Set up a writer to send messages to the client with automatic flushing */
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); ) {

                    logMessage("Client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                    String request;
                    while ((request = in.readLine()) != null) {
                        logMessage("Received request from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " - " + request);
                        String response = handleRequest(request);
                        logMessage("Sending response to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " - " + response);
                        out.println(response);
                    }

                    logMessage("Client disconnected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                } catch (IOException e) {
                    logMessage("Error handling client: " + e.getMessage());
                    e.printStackTrace();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String handleRequest(String request) {
        String[] parts = request.split(" ");
        if (parts.length < 2 || parts.length > 3) return "ERROR: Malformed request. Incorrect number of arguments";

        String command = parts[0].toUpperCase();
        String key = parts[1];

        switch (command) {
            case "PUT":
                if (parts.length != 3) return "ERROR: Malformed request. PUT requires key and value";
                String value = parts[2];
                store.put(key, value);
                return "PUT OK: " + key;

            case "GET":
                if (parts.length != 2) return "ERROR: Malformed request. GET requires key only";
                return store.containsKey(key) ? "VALUE: " + store.get(key) : "ERROR: Key not found";

            case "DELETE":
                if (parts.length != 2) return "ERROR: Malformed request. DELETE requires key only";
                return store.remove(key) != null ? "DELETE OK: " + key : "ERROR: Key not found";

            default:
                return "ERROR: Malformed request with unknown request type";
        }
    }

    public static void logMessage(String message) {
        System.out.println("[LOG] " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date()) + " - " + message);
    }

}
