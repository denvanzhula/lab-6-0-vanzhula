package ua.edu.chmnu.network.java.server;

import java.io.*;
import java.net.*;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientName;
    private BufferedReader input;
    private PrintWriter output;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            output.println("Enter your name:");
            clientName = input.readLine();
            if (clientName == null || clientName.trim().isEmpty()) {
                output.println("Invalid name. Connection closed.");
                clientSocket.close();
                return;
            }

            synchronized (TCPServer.class) {
                if (TCPServer.getClient(clientName) != null) {
                    output.println("Name already in use. Connection closed.");
                    clientSocket.close();
                    return;
                }
                TCPServer.addClient(clientName, this);
            }

            output.println("Welcome, " + clientName + "! You are now registered.");
            System.out.println(clientName + " connected.");

            String message;
            while ((message = input.readLine()) != null) {
                if (message.startsWith("@")) {
                    handlePrivateMessage(message);
                } else if (message.equalsIgnoreCase("list")) {
                    output.println("Connected clients: " + TCPServer.listClients());
                } else {
                    output.println("Invalid command. Use '@recipient message' or 'list'.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handlePrivateMessage(String message) {
        int spaceIndex = message.indexOf(" ");
        if (spaceIndex == -1) {
            output.println("Invalid message format. Use '@recipient message'.");
            return;
        }

        String recipientName = message.substring(1, spaceIndex);
        String actualMessage = message.substring(spaceIndex + 1);

        ClientHandler recipient = TCPServer.getClient(recipientName);
        if (recipient != null) {
            recipient.sendMessage(clientName + " says: " + actualMessage);
            output.println("Message sent to " + recipientName + ".");
            System.out.println(clientName + " -> " + recipientName + ": " + actualMessage);
        } else {
            output.println("User " + recipientName + " not found.");
        }
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    private void cleanup() {
        try {
            if (clientName != null) {
                TCPServer.removeClient(clientName);
                System.out.println(clientName + " disconnected.");
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}
