import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) {
        try (BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
            // Ask for server IP and port in a single line
            System.out.print("Enter server address (IP:Port): ");
            String[] address = consoleInput.readLine().split(":");

            if (address.length != 2) {
                System.out.println("Invalid format. Use IP:Port (e.g., 192.168.1.10:5000)");
                return;
            }

            String serverIP = address[0];
            int serverPort = Integer.parseInt(address[1]);

            // Connect to server
            try (Socket socket = new Socket(serverIP, serverPort)) {
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                // Ask for username
                System.out.print("Enter your name: ");
                String username = consoleInput.readLine();
                output.println(username);

                // Thread to receive messages
                new Thread(() -> {
                    try {
                        String serverMessage;
                        while ((serverMessage = input.readLine()) != null) {
                            System.out.println(serverMessage);
                        }
                    } catch (IOException e) {
                        System.out.println("Disconnected from server.");
                    }
                }).start();

                // Send messages
                String message;
                while ((message = consoleInput.readLine()) != null) {
                    output.println(message);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}