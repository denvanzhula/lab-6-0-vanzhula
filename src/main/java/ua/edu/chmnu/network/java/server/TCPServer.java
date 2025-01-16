package ua.edu.chmnu.network.java.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class TCPServer {
    private static final int PORT = 12345;
    private static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Server started... Waiting for clients to connect.");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected.");
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error in server: " + e.getMessage());
        }
    }

    public static void addClient(String clientName, ClientHandler clientHandler) {
        clients.put(clientName, clientHandler);
    }

    public static void removeClient(String clientName) {
        clients.remove(clientName);
    }

    public static ClientHandler getClient(String clientName) {
        return clients.get(clientName);
    }

    public static String listClients() {
        return String.join(", ", clients.keySet());
    }
}
