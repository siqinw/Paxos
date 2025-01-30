package UDP;
import java.net.*;
import java.io.*;
import java.util.HashMap;

import static TCP.TCPKeyValueStoreServer.*;


public class UDPKeyValueStoreServer {
    private static final HashMap<String, String> store = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java UDPKeyValueStoreServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (DatagramSocket socket = new DatagramSocket(port)) {
            logMessage("UDP Server started and listening on port: " + port);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String request = new String(packet.getData(), 0, packet.getLength()).trim();
                logMessage("Received request from " + packet.getAddress() + ":" + packet.getPort() + " - " + request);

                String response = handleRequest(request);
                logMessage("Sending response to " + packet.getAddress() + ":" + packet.getPort() + " - " + response);

                byte[] responseBytes = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                socket.send(responsePacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
