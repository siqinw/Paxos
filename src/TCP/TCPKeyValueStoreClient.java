package TCP;
import java.io.*;
import java.net.*;
import static TCP.TCPKeyValueStoreServer.logMessage;

public class TCPKeyValueStoreClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java TCPKeyValueStoreClient <server_address> <port>");
            return;
        }

        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);


        try (Socket socket = new Socket()) {
                /* Connect with a 5-second timeout */
             socket.connect(new InetSocketAddress(serverAddress, port), 5000);
                /* Set up a reader to receive messages from the server */
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                /* Set up a writer to send messages to the server with automatic flushing */
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                /* Set up a reader to receive user input from the console */
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            logMessage("Connected to server at "+serverAddress +":"+port);

            System.out.println("Enter commands (PUT <key> <value>, GET <key>, DELETE <key>) or 'exit' to quit.");

            String userInput;
            while(true) {
                logMessage("Waiting for user input");
                System.out.print("Command: ");
                userInput = consoleInput.readLine();
                if (userInput.equalsIgnoreCase("exit")) break;

                out.println(userInput);  // Send command to server
                String response;
                try {
                    socket.setSoTimeout(5000); /* Set a 5-second timeout for reading the response */
                    response = in.readLine();
                    if (response == null) throw new SocketTimeoutException();
                } catch (SocketTimeoutException e) {
                    response = "ERROR: Server timeout or no response";
                }

                logMessage("Response: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
