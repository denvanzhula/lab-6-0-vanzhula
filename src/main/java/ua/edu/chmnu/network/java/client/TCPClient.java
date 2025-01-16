package ua.edu.chmnu.network.java.client;

import java.io.*;
import java.net.Socket;

public class TCPClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            String serverMessage = serverInput.readLine();
            if (serverMessage.equals("Enter your name:")) {
                System.out.println(serverMessage);
                String name = userInput.readLine();
                serverOutput.println(name);
                System.out.println("Registered successfully as " + name);
            }

            new Thread(() -> {
                try {
                    String messageFromServer;
                    while ((messageFromServer = serverInput.readLine()) != null) {
                        System.out.println("Server: " + messageFromServer);
                    }
                } catch (IOException e) {
                    System.err.println("Connection lost: " + e.getMessage());
                }
            }).start();

            while (true) {
                System.out.println("Enter a command (send/exit):");
                String command = userInput.readLine();

                if ("send".equalsIgnoreCase(command)) {
                    System.out.println("Enter the name of the recipient:");
                    String recipient = userInput.readLine();
                    System.out.println("Enter your message:");
                    String message = userInput.readLine();
                    serverOutput.println("@" + recipient + " " + message);
                } else if ("exit".equalsIgnoreCase(command)) {
                    System.out.println("Exiting...");
                    break;
                } else {
                    System.out.println("Invalid command. Use 'send' or 'exit'.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}