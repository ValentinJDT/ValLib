package fr.valentin.lib.vallib.java.tcp.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class ClientSocketHandler {
    private final String serverIp;
    private final int serverPort;
    private final int maxRetries;
    private final long retryTimeout;
    private UUID clientUUID;
    private Socket socket;

    public ClientSocketHandler(String serverIp, int serverPort, int maxRetries, long retryTimeout) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.maxRetries = maxRetries;
        this.retryTimeout = retryTimeout;
    }

    public ClientSocketHandler(String serverIp, int serverPort) {
        this(serverIp, serverPort, 3, 1000L);
    }

    public UUID getUuid() {
        return clientUUID;
    }

    public void connect() {
        int attempts = 0;
        while (attempts < maxRetries) {
            if (tryConnecting()) {
                return;
            }
            attempts++;
            handleConnectionFailure(attempts);
        }
    }

    private boolean tryConnecting() {
        try {
            socket = new Socket(serverIp, serverPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientUUID = UUID.fromString(reader.readLine());
            System.out.println("Connected to server with UUID: " + clientUUID);
            new Thread(this::listenToServer).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void handleConnectionFailure(int attempts) {
        System.out.println("Connection failed. Attempt " + attempts + "/" + maxRetries + ".");
        if (attempts < maxRetries) {
            try {
                Thread.sleep(retryTimeout);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.println("Failed to connect after " + maxRetries + " attempts.");
        }
    }

    private void listenToServer() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            reader.lines().forEach(this::handleMessage);
        } catch (IOException e) {
            System.out.println("Error reading messages from server: " + e.getMessage());
        }
    }

    private void handleMessage(String message) {
        if ("KICK".equals(message)) {
            System.out.println("Kicked from server.");
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
            return;
        }
        System.out.println(message);
    }

    public void sendMessage(String message) {
        createWriter().println(message);
    }

    public void sendMessageToClient(UUID targetUUID, String message) {
        sendMessage("TO " + targetUUID + ": " + message);
    }

    public void sendBroadcastMessage(String message) {
        sendMessage("GLOBAL: " + message);
    }

    private PrintWriter createWriter() {
        try {
            return new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException("Error creating writer: " + e.getMessage());
        }
    }

    public void disconnect() {
        sendMessage("DISCONNECT");
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
        System.out.println("Disconnected from server.");
    }
}