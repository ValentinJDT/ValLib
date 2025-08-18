package fr.valentinjdt.lib.java.tcp.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ServerSocketHandler class is responsible for managing server-side socket connections.
 * It handles client connections, disconnections, and message processing.
 * 
 * <p>
 * This class provides methods to start and stop the server, subscribe to messages,
 * handle client connections and disconnections, and send messages to clients.
 * </p>
 * 
 * <p>
 * The server listens for incoming client connections on a specified port and
 * manages connected clients using UUIDs. It supports broadcasting messages to
 * all clients and sending messages to specific clients.
 * </p>
 * 
 * <p>
 * The class also provides callback mechanisms for client connection and disconnection events.
 * </p>
 * 
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * ServerSocketHandler server = new ServerSocketHandler(12345);
 * server.start();
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * Note: The server runs in a separate thread for each client connection.
 * </p>
 * 
 * @see java.net.ServerSocket
 * @see java.net.Socket
 * @see java.util.UUID
 * @see java.util.concurrent.ConcurrentHashMap
 * @see java.util.function.BiConsumer
 * @see java.util.function.Consumer
 */
public class ServerSocketHandler {
    private static final Logger LOGGER = Logger.getLogger(ServerSocketHandler.class.getName());
    private static final String DELIMITER = ":";
    private final int port;
    private final ConcurrentHashMap<UUID, Socket> clientMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, PrintWriter> writerMap = new ConcurrentHashMap<>();
    private final List<BiConsumer<UUID, String>> subscribers = new CopyOnWriteArrayList<>();
    private final List<Consumer<UUID>> connectCallbacks = new CopyOnWriteArrayList<>();
    private final List<Consumer<UUID>> disconnectCallbacks = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket = null;
    private volatile boolean isRunning = true;

    public ServerSocketHandler(int port) {
        this.port = port;
    }

    /**
     * Subscribes a callback to be notified when a new message is received.
     *
     * @param callback the callback function to be invoked with the UUID of the sender and the message string.
     */
    public void subscribe(BiConsumer<UUID, String> callback) {
        subscribers.add(callback);
    }

    /**
     * Registers a callback to be executed when a client connects.
     *
     * @param callback the callback function to be executed, which accepts a UUID representing the client ID
     */
    public void onClientConnect(Consumer<UUID> callback) {
        connectCallbacks.add(callback);
    }

    /**
     * Registers a callback to be invoked when a client disconnects.
     *
     * @param callback the callback to be executed, which accepts a UUID representing the disconnected client
     */
    public void onClientDisconnect(Consumer<UUID> callback) {
        disconnectCallbacks.add(callback);
    }

    /**
     * Starts the server and listens for incoming client connections.
     * This method initializes the server socket on the specified port and
     * continuously accepts client connections while the server is running.
     * Each client connection is handled in a separate thread.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            LOGGER.info("Server started on port " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                UUID clientUUID = addClient(clientSocket);

                new Thread(() -> {
                    try {
                        handleClient(clientUUID, clientSocket);
                    } catch (Exception e) {
                        LOGGER.info("Client disconnected: " + clientUUID);
                        removeClient(clientUUID, clientSocket);
                    }
                }).start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server error", e);
        }
    }

    /**
     * Stops the server by setting the running flag to false, kicking all connected clients,
     * and closing the server socket if it is not null. Logs any IOException that occurs
     * during the closing of the server socket and logs an info message when the server is stopped.
     */
    public void stop() {
        isRunning = false;
        clientMap.keySet().forEach(this::kickClient);
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Server error", e);
            }
        }
        LOGGER.info("Server stopped.");
    }

    /**
     * Adds a client to the server by generating a unique UUID for the client,
     * storing the client's socket and output stream writer in respective maps,
     * and notifying all registered connection callbacks.
     *
     * @param clientSocket the socket of the client to be added
     * @return the UUID assigned to the connected client
     */
    private UUID addClient(Socket clientSocket) {
        UUID clientUUID = UUID.randomUUID();
        clientMap.put(clientUUID, clientSocket);
        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writerMap.put(clientUUID, writer);
            sendToClient(clientUUID, clientUUID.toString());
            connectCallbacks.forEach(cb -> cb.accept(clientUUID));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        LOGGER.info("Client connected: " + clientUUID);
        return clientUUID;
    }

    /**
     * Handles communication with a client connected to the server.
     *
     * @param uuid   the unique identifier of the client
     * @param socket the socket associated with the client connection
     */
    private void handleClient(UUID uuid, Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            reader.lines().forEach(message -> processMessage(uuid, socket, message));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Message parsing not working", e);
        }
    }

    /**
     * Processes a message received from a client identified by a UUID.
     *
     * @param uuid    The UUID of the client sending the message.
     * @param socket  The socket associated with the client.
     * @param message The message received from the client.
     *
     * The message is processed based on its type:
     * - "TO": Sends a message to a specific client identified by a UUID.
     * - "GLOBAL": Broadcasts a message to all connected clients.
     * - "DISCONNECT": Handles client disconnection.
     * - Default: Logs the message and notifies subscribers.
     */
    private void processMessage(UUID uuid, Socket socket, String message) {
        switch (message.split(DELIMITER, 2)[0]) {
            case "TO" -> {
                String targetUUIDMessage = message.substring("TO".length()).trim();
                String targetUUID = targetUUIDMessage.substring(0, targetUUIDMessage.indexOf(':')).trim();
                String actualMessage = targetUUIDMessage.substring(targetUUIDMessage.indexOf(':') + 1).trim();

                try {
                    UUID targetUUIDParsed = UUID.fromString(targetUUID);
                    sendToClient(targetUUIDParsed, "FROM " + uuid + ": " + actualMessage);
                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING, "Invalid UUID format from client {0}.", uuid);
                    sendToClient(uuid, "Invalid UUID format.");
                }
            }
            case "GLOBAL" -> {
                String actualMessage = message.substring("GLOBAL:".length()).trim();
                broadcastMessage(uuid, actualMessage);
            }
            case "DISCONNECT" -> {
                LOGGER.log(Level.INFO, "Client {0} requested disconnection.", uuid);
                removeClient(uuid, socket);
            }
            default -> {
                LOGGER.info("Received from " + uuid + ": " + message);
                subscribers.forEach(c -> c.accept(uuid, message));
            }
        }
    }

    /**
     * Removes a client from the server based on the provided UUID and closes the associated socket.
     * 
     * @param uuid   the unique identifier of the client to be removed
     * @param socket the socket associated with the client to be closed
     */
    private void removeClient(UUID uuid, Socket socket) {
        clientMap.remove(uuid);
        PrintWriter writer = writerMap.remove(uuid);
        if (writer != null) {
            writer.close();
        }
        disconnectCallbacks.forEach(cb -> cb.accept(uuid));
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending message to client " + uuid + ".", e);
            clientMap.remove(uuid);
        }
    }

    /**
     * Sends a message to the client identified by the given UUID.
     *
     * @param uuid    the UUID of the client to send the message to
     * @param message the message to be sent to the client
     */
    public void sendToClient(UUID uuid, String message) {
        Socket clientSocket = clientMap.get(uuid);
        if (clientSocket != null) {
            PrintWriter writer = writerMap.get(uuid);
            if (writer != null) {
                writer.println(message);
            } else {
                LOGGER.warning("Client writer for " + uuid + " not found or disconnected.");
            }
        }
    }

    /**
     * Broadcasts a message to all connected clients except the sender.
     *
     * @param senderUUID the UUID of the client sending the message
     * @param message    the message to be broadcasted
     */
    private void broadcastMessage(UUID senderUUID, String message) {
        clientMap.keySet().forEach(targetUUID -> {
            if (!targetUUID.equals(senderUUID)) {
                sendToClient(targetUUID, "GLOBAL " + senderUUID + ": " + message);
            }
        });
    }

    /**
     * Kicks a client from the server based on their UUID.
     * <p>
     * This method sends a "KICK" message to the client, removes the client from the
     * client map,
     * and closes the client's socket connection. If the client is not found or
     * already disconnected,
     * a warning is logged.
     * </p>
     *
     * @param uuid the UUID of the client to be kicked
     */
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
            LOGGER.log(Level.INFO, "Client {0} has been kicked.", uuid);
        } else {
            LOGGER.log(Level.WARNING, "Client {0} not found or already disconnected.", uuid);
        }
    }
}