package fr.valentin.lib.vallib.java.tcp.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSocketHandler {
    private final int port;
    private final ConcurrentHashMap<UUID, Socket> clientMap = new ConcurrentHashMap<>();
    private ServerSocket serverSocket = null;
    private volatile boolean isRunning = true;

    public ServerSocketHandler(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                UUID clientUUID = addClient(clientSocket);

                new Thread(() -> handleClient(clientUUID, clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        isRunning = false;
        clientMap.keySet().forEach(this::kickClient);
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Server stopped.");
    }

    private UUID addClient(Socket clientSocket) {
        UUID clientUUID = UUID.randomUUID();
        clientMap.put(clientUUID, clientSocket);
        sendToClient(clientUUID, clientUUID.toString());
        System.out.println("Client connected: " + clientUUID);
        return clientUUID;
    }

    private void handleClient(UUID uuid, Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            reader.lines().forEach(message -> processMessage(uuid, socket, message));
        } catch (IOException e) {
            System.out.println("Client " + uuid + " disconnected.");
        } finally {
            removeClient(uuid, socket);
        }
    }

    private void processMessage(UUID uuid, Socket socket, String message) {
        switch (message.split(":", 2)[0]) {
            case "TO" -> {
                String targetUUIDMessage = message.substring("TO".length()).trim();
                String targetUUID = targetUUIDMessage.substring(0, targetUUIDMessage.indexOf(':')).trim();
                String actualMessage = targetUUIDMessage.substring(targetUUIDMessage.indexOf(':') + 1).trim();

                try {
                    UUID targetUUIDParsed = UUID.fromString(targetUUID);
                    sendToClient(targetUUIDParsed, "FROM " + uuid + ": " + actualMessage);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid UUID format from client " + uuid + ".");
                    sendToClient(uuid, "Invalid UUID format.");
                }
            }
            case "GLOBAL" -> {
                String actualMessage = message.substring("GLOBAL:".length()).trim();
                broadcastMessage(uuid, actualMessage);
            }
            case "DISCONNECT" -> {
                System.out.println("Client " + uuid + " requested disconnection.");
                removeClient(uuid, socket);
            }
            default -> System.out.println("Received from " + uuid + ": " + message);
        }
    }

    private void removeClient(UUID uuid, Socket socket) {
        clientMap.remove(uuid);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToClient(UUID uuid, String message) {
        Socket clientSocket = clientMap.get(uuid);
        if (clientSocket != null) {
            try {
                new PrintWriter(clientSocket.getOutputStream(), true).println(message);
            } catch (IOException e) {
                System.out.println("Error sending message to client " + uuid + ".");
            }
        } else {
            System.out.println("Client " + uuid + " not found or disconnected.");
        }
    }

    private void broadcastMessage(UUID senderUUID, String message) {
        clientMap.keySet().forEach(targetUUID -> {
            if (!targetUUID.equals(senderUUID)) {
                sendToClient(targetUUID, "GLOBAL " + senderUUID + ": " + message);
            }
        });
    }

    public void kickClient(UUID uuid) {
        Socket clientSocket = clientMap.get(uuid);
        if (clientSocket != null) {
            sendToClient(uuid, "KICK");
            clientMap.remove(uuid);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Client " + uuid + " has been kicked.");
        } else {
            System.out.println("Client " + uuid + " not found or already disconnected.");
        }
    }
}