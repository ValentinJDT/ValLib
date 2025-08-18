package fr.valentinjdt.lib.java.tcp.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Handles the client-side socket connection to a server.
 * 
 * <p>This class manages the connection to a server, including retrying connections,
 * handling incoming messages, and sending messages to the server. It also supports
 * subscribing to incoming messages and handling kick events from the server.</p>
 * 
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ClientSocketHandler client = new ClientSocketHandler("127.0.0.1", 8080);
 * client.connect();
 * client.subscribe(message -> System.out.println("Received: " + message));
 * client.onKick(() -> System.out.println("Kicked from server"));
 * client.sendMessage("Hello, Server!");
 * }
 * </pre>
 * 
 * <p>Constructor parameters:</p>
 * <ul>
 *   <li>{@code serverIp} - The IP address of the server to connect to.</li>
 *   <li>{@code serverPort} - The port number of the server to connect to.</li>
 *   <li>{@code maxRetries} - The maximum number of connection attempts before giving up.</li>
 *   <li>{@code retryTimeout} - The time to wait between connection attempts in milliseconds.</li>
 * </ul>
 * 
 * <p>Public methods:</p>
 * <ul>
 *   <li>{@link #getUuid()} - Returns the UUID assigned by the server.</li>
 *   <li>{@link #connect()} - Attempts to connect to the server, retrying if necessary.</li>
 *   <li>{@link #sendMessage(String)} - Sends a message to the server.</li>
 *   <li>{@link #sendMessageToClient(UUID, String)} - Sends a message to a specific client via the server.</li>
 *   <li>{@link #sendBroadcastMessage(String)} - Sends a broadcast message to all clients via the server.</li>
 *   <li>{@link #subscribe(Consumer)} - Subscribes to incoming messages from the server.</li>
 *   <li>{@link #onKick(Runnable)} - Registers a callback to be executed when kicked from the server.</li>
 *   <li>{@link #disconnect()} - Disconnects from the server.</li>
 * </ul>
 */
public class ClientSocketHandler {
    private static final Logger LOGGER = Logger.getLogger(ClientSocketHandler.class.getName());

    private final String serverIp;
    private final int serverPort;
    private final int maxRetries;
    private final long retryTimeout;
    private UUID clientUUID;
    private Socket socket;
    private final List<Consumer<String>> subscribers = new ArrayList<>();
    private final List<Runnable> kickCallbacks = new ArrayList<>();

    public ClientSocketHandler(String serverIp, int serverPort, int maxRetries, long retryTimeout) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.maxRetries = maxRetries;
        this.retryTimeout = retryTimeout;
    }

    public ClientSocketHandler(String serverIp, int serverPort) {
        this(serverIp, serverPort, 3, 1000L);
    }

    /**
     * Retrieves the UUID of the client.
     *
     * @return the UUID of the client.
     */
    public UUID getUuid() {
        return clientUUID;
    }

    /**
     * Attempts to establish a connection, retrying up to a maximum number of times specified by {@code maxRetries}.
     * If the connection is successful, the method returns immediately.
     * If the connection fails, it increments the attempt counter and handles the failure.
     */
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

    /**
     * Attempts to establish a connection to the server using the specified server IP and port.
     * If the connection is successful, it reads the UUID from the server, logs the connection,
     * and starts a new thread to listen to the server.
     *
     * @return true if the connection is successfully established, false otherwise.
     */
    private boolean tryConnecting() {
        try {
            socket = new Socket(serverIp, serverPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientUUID = UUID.fromString(reader.readLine());
            LOGGER.info("Connected to server with UUID: " + clientUUID);
            new Thread(this::listenToServer).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Handles the connection failure by logging the attempt and retrying if the maximum number of retries has not been reached.
     * If the maximum number of retries is reached, logs a severe message.
     *
     * @param attempts the current number of connection attempts
     */
    private void handleConnectionFailure(int attempts) {
        LOGGER.warning("Connection failed. Attempt " + attempts + "/" + maxRetries + ".");
        if (attempts < maxRetries) {
            try {
                Thread.sleep(retryTimeout);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            LOGGER.severe("Failed to connect after " + maxRetries + " attempts.");
        }
    }

    /**
     * Listens for messages from the server. This method reads messages from the server
     * using a BufferedReader and processes each message by passing it to the handleMessage method.
     * If an IOException occurs while reading messages, an error message is logged.
     */
    private void listenToServer() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            reader.lines().forEach(this::handleMessage);
        } catch (IOException e) {
            LOGGER.severe("Error reading messages from server: " + e.getMessage());
        }
    }

    /**
     * Handles incoming messages from the server.
     * If the message is "KICK", it runs all kick callbacks, logs a warning,
     * and attempts to close the socket. If an IOException occurs during socket
     * closure, it logs a severe error.
     * For other messages, it logs the message and notifies all subscribers.
     *
     * @param message the message received from the server
     */
    private void handleMessage(String message) {
        if ("KICK".equals(message)) {
            kickCallbacks.forEach(Runnable::run);
            LOGGER.warning("Kicked from server.");
            try {
                socket.close();
            } catch (IOException e) {
                LOGGER.severe("Error closing socket: " + e.getMessage());
            }
            return;
        }
        LOGGER.info(message);
        subscribers.forEach(subscriber -> subscriber.accept(message));
    }

    /**
     * Sends a message to the connected socket.
     *
     * @param message the message to be sent
     */
    public void sendMessage(String message) {
        createWriter().println(message);
    }

    /**
     * Sends a message to a client identified by the given UUID.
     *
     * @param targetUUID the UUID of the target client
     * @param message the message to be sent to the client
     */
    public void sendMessageToClient(UUID targetUUID, String message) {
        sendMessage("TO " + targetUUID + ": " + message);
    }

    /**
     * Sends a broadcast message to all connected clients.
     *
     * @param message the message to be broadcasted
     */
    public void sendBroadcastMessage(String message) {
        sendMessage("GLOBAL: " + message);
    }

    /**
     * Subscribes a callback to be notified with a string message.
     *
     * @param callback the callback function to be added to the list of subscribers
     */
    public void subscribe(Consumer<String> callback) {
        subscribers.add(callback);
    }

    /**
     * Registers a callback to be executed when the client is kicked.
     *
     * @param callback the Runnable to be executed upon being kicked
     */
    public void onKick(Runnable callback) {
        kickCallbacks.add(callback);
    }

    /**
     * Creates a PrintWriter for the socket's output stream.
     * The PrintWriter is configured to automatically flush the output buffer.
     *
     * @return a PrintWriter object for the socket's output stream
     * @throws RuntimeException if an I/O error occurs when creating the writer
     */
    private PrintWriter createWriter() {
        try {
            return new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException("Error creating writer: " + e.getMessage());
        }
    }

    /**
     * Disconnects the client from the server by sending a "DISCONNECT" message
     * and closing the socket. Logs an error message if an IOException occurs
     * while closing the socket, and logs an info message upon successful disconnection.
     */
    public void disconnect() {
        sendMessage("DISCONNECT");
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.severe("Error closing socket: " + e.getMessage());
        }
        LOGGER.info("Disconnected from server.");
    }
}