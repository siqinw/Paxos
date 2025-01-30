package UDP;

import java.net.*;
import java.io.*;

import static TCP.TCPKeyValueStoreServer.logMessage;

public class UDPKeyValueStoreClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java UDPKeyValueStoreClient <server_address> <port>");
            return;
        }

        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);

        try (DatagramSocket socket = new DatagramSocket();
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Enter commands (PUT <key> <value>, GET <key>, DELETE <key>) or 'exit' to quit.");
            String userInput;

            while (true) {
                logMessage("Waiting for user input");
                System.out.print("Command: ");
                userInput = consoleInput.readLine();
                if (userInput.equalsIgnoreCase("exit")) break;

                byte[] requestBytes = userInput.getBytes();
                DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, InetAddress.getByName(serverAddress), port);
                socket.send(requestPacket);

                byte[] buffer = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                socket.setSoTimeout(5000); // Set 5-second timeout for response

                try {
                    socket.receive(responsePacket);
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength()).trim();
                    logMessage("Response: " + response);
                } catch (SocketTimeoutException e) {
                    logMessage("Request timed out. No response from server.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
