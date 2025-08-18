package fr.valentinjdt.lib.tcp.connection

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger


/**
 * The ServerSocketHandler class is responsible for managing server-side socket connections.
 * It handles client connections, disconnections, and message processing.
 *
 *
 *
 * This class provides methods to start and stop the server, subscribe to messages,
 * handle client connections and disconnections, and send messages to clients.
 *
 *
 *
 *
 * The server listens for incoming client connections on a specified port and
 * manages connected clients using UUIDs. It supports broadcasting messages to
 * all clients and sending messages to specific clients.
 *
 *
 *
 *
 * The class also provides callback mechanisms for client connection and disconnection events.
 *
 *
 *
 *
 * Example usage:
 * <pre>
 * `ServerSocketHandler server = new ServerSocketHandler(12345);
 * server.start();
` *
</pre> *
 *
 *
 *
 *
 * Note: The server runs in a separate thread for each client connection.
 *
 *
 * @see ServerSocket
 *
 * @see Socket
 *
 * @see UUID
 *
 * @see ConcurrentHashMap
 *
 * @see BiConsumer
 *
 * @see Consumer
 */
class ServerSocketHandler(private val port: Int) {
    private val clientMap = ConcurrentHashMap<UUID?, Socket?>()
    private val writerMap = ConcurrentHashMap<UUID?, PrintWriter?>()
    private val subscribers: MutableList<(UUID, String) -> Unit> =
        CopyOnWriteArrayList()
    private val connectCallbacks: MutableList<(UUID) -> Unit> = CopyOnWriteArrayList()
    private val disconnectCallbacks: MutableList<(UUID) -> Unit> = CopyOnWriteArrayList()
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var isRunning = true

    /**
     * Subscribes a callback to be notified when a new message is received.
     *
     * @param callback the callback function to be invoked with the UUID of the sender and the message string.
     */
    fun subscribe(callback: (UUID, String) -> Unit) {
        subscribers.add(callback)
    }

    /**
     * Registers a callback to be executed when a client connects.
     *
     * @param callback the callback function to be executed, which accepts a UUID representing the client ID
     */
    fun onClientConnect(callback: (UUID) -> Unit) {
        connectCallbacks.add(callback)
    }

    /**
     * Registers a callback to be invoked when a client disconnects.
     *
     * @param callback the callback to be executed, which accepts a UUID representing the disconnected client
     */
    fun onClientDisconnect(callback: (UUID) -> Unit) {
        disconnectCallbacks.add(callback)
    }

    /**
     * Starts the server and listens for incoming client connections.
     * This method initializes the server socket on the specified port and
     * continuously accepts client connections while the server is running.
     * Each client connection is handled in a separate thread.
     *
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    fun start() {
        try {
            serverSocket = ServerSocket(port)
            LOGGER.info("Server started on port $port")

            while (isRunning) {
                val clientSocket = serverSocket!!.accept()
                val clientUUID = addClient(clientSocket)

                Thread(Runnable {
                    try {
                        handleClient(clientUUID, clientSocket)
                    } catch (e: Exception) {
                        LOGGER.info("Client disconnected: $clientUUID")
                        removeClient(clientUUID, clientSocket)
                    }
                }).start()
            }
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, "Server error", e)
        }
    }

    /**
     * Stops the server by setting the running flag to false, kicking all connected clients,
     * and closing the server socket if it is not null. Logs any IOException that occurs
     * during the closing of the server socket and logs an info message when the server is stopped.
     */
    fun stop() {
        isRunning = false
        clientMap.keys.forEach(Consumer { uuid: UUID? -> this.kickClient(uuid!!) })
        if (serverSocket != null) {
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                LOGGER.log(Level.SEVERE, "Server error", e)
            }
        }
        LOGGER.info("Server stopped.")
    }

    /**
     * Adds a client to the server by generating a unique UUID for the client,
     * storing the client's socket and output stream writer in respective maps,
     * and notifying all registered connection callbacks.
     *
     * @param clientSocket the socket of the client to be added
     * @return the UUID assigned to the connected client
     */
    private fun addClient(clientSocket: Socket): UUID {
        val clientUUID = UUID.randomUUID()
        clientMap.put(clientUUID, clientSocket)
        try {
            val writer = PrintWriter(clientSocket.getOutputStream(), true)
            writerMap.put(clientUUID, writer)
            sendToClient(clientUUID, clientUUID.toString())
            connectCallbacks.forEach { cb -> cb(clientUUID) }
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, e.message, e)
        }
        LOGGER.info("Client connected: $clientUUID")
        return clientUUID
    }

    /**
     * Handles communication with a client connected to the server.
     *
     * @param uuid   the unique identifier of the client
     * @param socket the socket associated with the client connection
     */
    private fun handleClient(uuid: UUID, socket: Socket) {
        try {
            BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
                reader.lines().forEach { message: String? -> processMessage(uuid, socket, message!!) }
            }
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, "Message parsing not working", e)
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
    private fun processMessage(uuid: UUID, socket: Socket, message: String) {
        when (message.split(DELIMITER.toRegex(), limit = 2).toTypedArray()[0]) {
            "TO" -> {
                val targetUUIDMessage = message.substring("TO".length).trim { it <= ' ' }
                val targetUUID = targetUUIDMessage.substring(0, targetUUIDMessage.indexOf(':')).trim { it <= ' ' }
                val actualMessage = targetUUIDMessage.substring(targetUUIDMessage.indexOf(':') + 1).trim { it <= ' ' }

                try {
                    val targetUUIDParsed = UUID.fromString(targetUUID)
                    sendToClient(targetUUIDParsed, "FROM $uuid: $actualMessage")
                } catch (e: IllegalArgumentException) {
                    LOGGER.log(Level.WARNING, "Invalid UUID format from client $uuid.")
                    sendToClient(uuid, "Invalid UUID format.")
                }
            }

            "GLOBAL" -> {
                val actualMessage = message.substring("GLOBAL:".length).trim { it <= ' ' }
                broadcastMessage(uuid, actualMessage)
            }

            "DISCONNECT" -> {
                LOGGER.log(Level.INFO, "Client $uuid requested disconnection.")
                removeClient(uuid, socket)
            }

            else -> {
                LOGGER.info("Received from $uuid: $message")
                subscribers.forEach{ cb -> cb(uuid, message) }
            }
        }
    }

    /**
     * Removes a client from the server based on the provided UUID and closes the associated socket.
     *
     * @param uuid   the unique identifier of the client to be removed
     * @param socket the socket associated with the client to be closed
     */
    private fun removeClient(uuid: UUID, socket: Socket) {
        clientMap.remove(uuid)
        val writer = writerMap.remove(uuid)
        if (writer != null) {
            writer.close()
        }
        disconnectCallbacks.forEach { cb -> cb(uuid) }
        try {
            socket.close()
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, "Error sending message to client $uuid.", e)
            clientMap.remove(uuid)
        }
    }

    /**
     * Sends a message to the client identified by the given UUID.
     *
     * @param uuid    the UUID of the client to send the message to
     * @param message the message to be sent to the client
     */
    fun sendToClient(uuid: UUID?, message: String?) {
        val clientSocket = clientMap.get(uuid)
        if (clientSocket != null) {
            val writer = writerMap.get(uuid)
            if (writer != null) {
                writer.println(message)
            } else {
                LOGGER.warning("Client writer for $uuid not found or disconnected.")
            }
        }
    }

    /**
     * Broadcasts a message to all connected clients except the sender.
     *
     * @param senderUUID the UUID of the client sending the message
     * @param message    the message to be broadcasted
     */
    private fun broadcastMessage(senderUUID: UUID?, message: String?) {
        clientMap.keys.forEach(Consumer { targetUUID: UUID? ->
            if (targetUUID != senderUUID) {
                sendToClient(targetUUID, "GLOBAL $senderUUID: $message")
            }
        })
    }

    /**
     * Kicks a client from the server based on their UUID.
     *
     *
     * This method sends a "KICK" message to the client, removes the client from the
     * client map,
     * and closes the client's socket connection. If the client is not found or
     * already disconnected,
     * a warning is logged.
     *
     *
     * @param uuid the UUID of the client to be kicked
     */
    fun kickClient(uuid: UUID) {
        val clientSocket = clientMap.get(uuid)
        if (clientSocket != null) {
            sendToClient(uuid, "KICK")
            clientMap.remove(uuid)
            try {
                clientSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            LOGGER.log(Level.INFO, "Client {0} has been kicked.", uuid)
        } else {
            LOGGER.log(Level.WARNING, "Client {0} not found or already disconnected.", uuid)
        }
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger(ServerSocketHandler::class.java.getName())
        private const val DELIMITER = ":"
    }
}

/**
 * Handles the client-side socket connection to a server.
 *
 *
 * This class manages the connection to a server, including retrying connections,
 * handling incoming messages, and sending messages to the server. It also supports
 * subscribing to incoming messages and handling kick events from the server.
 *
 *
 * Usage example:
 * <pre>
 * `ClientSocketHandler client = new ClientSocketHandler("127.0.0.1", 8080);
 * client.connect();
 * client.subscribe(message -> System.out.println("Received: " + message));
 * client.onKick(() -> System.out.println("Kicked from server"));
 * client.sendMessage("Hello, Server!");
` *
</pre> *
 *
 *
 * Constructor parameters:
 *
 *  * `serverIp` - The IP address of the server to connect to.
 *  * `serverPort` - The port number of the server to connect to.
 *  * `maxRetries` - The maximum number of connection attempts before giving up.
 *  * `retryTimeout` - The time to wait between connection attempts in milliseconds.
 *
 *
 *
 * Public methods:
 *
 *  * [.getUuid] - Returns the UUID assigned by the server.
 *  * [.connect] - Attempts to connect to the server, retrying if necessary.
 *  * [.sendMessage] - Sends a message to the server.
 *  * [.sendMessageToClient] - Sends a message to a specific client via the server.
 *  * [.sendBroadcastMessage] - Sends a broadcast message to all clients via the server.
 *  * [.subscribe] - Subscribes to incoming messages from the server.
 *  * [.onKick] - Registers a callback to be executed when kicked from the server.
 *  * [.disconnect] - Disconnects from the server.
 *
 */
class ClientSocketHandler(
    private val serverIp: String,
    private val serverPort: Int,
    private val maxRetries: Int = 3,
    private val retryTimeout: Long = 1000L
) {
    /**
     * Retrieves the UUID of the client.
     *
     * @return the UUID of the client.
     */
    var uuid: UUID? = null
        private set
    private var socket: Socket? = null
    private val subscribers: MutableList<(String?) -> Unit> = ArrayList()
    private val kickCallbacks: MutableList<() -> Unit> = ArrayList()

    /**
     * Attempts to establish a connection, retrying up to a maximum number of times specified by `maxRetries`.
     * If the connection is successful, the method returns immediately.
     * If the connection fails, it increments the attempt counter and handles the failure.
     */
    fun connect() {
        var attempts = 0
        while (attempts < maxRetries) {
            if (tryConnecting()) {
                return
            }
            attempts++
            handleConnectionFailure(attempts)
        }
    }

    /**
     * Attempts to establish a connection to the server using the specified server IP and port.
     * If the connection is successful, it reads the UUID from the server, logs the connection,
     * and starts a new thread to listen to the server.
     *
     * @return true if the connection is successfully established, false otherwise.
     */
    private fun tryConnecting(): Boolean {
        try {
            socket = Socket(serverIp, serverPort)
            val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            this.uuid = UUID.fromString(reader.readLine())
            LOGGER.info("Connected to server with UUID: " + this.uuid)
            Thread(Runnable { this.listenToServer() }).start()
            return true
        } catch (e: IOException) {
            return false
        }
    }

    /**
     * Handles the connection failure by logging the attempt and retrying if the maximum number of retries has not been reached.
     * If the maximum number of retries is reached, logs a severe message.
     *
     * @param attempts the current number of connection attempts
     */
    private fun handleConnectionFailure(attempts: Int) {
        LOGGER.warning("Connection failed. Attempt $attempts/$maxRetries.")
        if (attempts < maxRetries) {
            try {
                Thread.sleep(retryTimeout)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        } else {
            LOGGER.severe("Failed to connect after $maxRetries attempts.")
        }
    }

    /**
     * Listens for messages from the server. This method reads messages from the server
     * using a BufferedReader and processes each message by passing it to the handleMessage method.
     * If an IOException occurs while reading messages, an error message is logged.
     */
    private fun listenToServer() {
        try {
            val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            reader.lines().forEach { message: String? -> this.handleMessage(message) }
        } catch (e: IOException) {
            LOGGER.severe("Error reading messages from server: " + e.message)
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
    private fun handleMessage(message: String?) {
        if ("KICK" == message) {
            kickCallbacks.forEach { Runnable::run }
            LOGGER.warning("Kicked from server.")
            try {
                socket!!.close()
            } catch (e: IOException) {
                LOGGER.severe("Error closing socket: ${e.message}")
            }
            return
        }
        LOGGER.info(message)
        subscribers.forEach { cb -> cb(message) }
    }

    /**
     * Sends a message to the connected socket.
     *
     * @param message the message to be sent
     */
    fun sendMessage(message: String?) {
        createWriter().println(message)
    }

    /**
     * Sends a message to a client identified by the given UUID.
     *
     * @param targetUUID the UUID of the target client
     * @param message the message to be sent to the client
     */
    fun sendMessageToClient(targetUUID: UUID?, message: String?) {
        sendMessage("TO $targetUUID: $message")
    }

    /**
     * Sends a broadcast message to all connected clients.
     *
     * @param message the message to be broadcasted
     */
    fun sendBroadcastMessage(message: String?) {
        sendMessage("GLOBAL: " + message)
    }

    /**
     * Subscribes a callback to be notified with a string message.
     *
     * @param callback the callback function to be added to the list of subscribers
     */
    fun subscribe(callback: (String?) -> Unit) {
        subscribers.add(callback)
    }

    /**
     * Registers a callback to be executed when the client is kicked.
     *
     * @param callback the Runnable to be executed upon being kicked
     */
    fun onKick(callback: () -> Unit) {
        kickCallbacks.add(callback)
    }

    /**
     * Creates a PrintWriter for the socket's output stream.
     * The PrintWriter is configured to automatically flush the output buffer.
     *
     * @return a PrintWriter object for the socket's output stream
     * @throws RuntimeException if an I/O error occurs when creating the writer
     */
    private fun createWriter(): PrintWriter {
        try {
            return PrintWriter(socket!!.getOutputStream(), true)
        } catch (e: IOException) {
            throw RuntimeException("Error creating writer: ${e.message}")
        }
    }

    /**
     * Disconnects the client from the server by sending a "DISCONNECT" message
     * and closing the socket. Logs an error message if an IOException occurs
     * while closing the socket, and logs an info message upon successful disconnection.
     */
    fun disconnect() {
        sendMessage("DISCONNECT")
        try {
            socket!!.close()
        } catch (e: IOException) {
            LOGGER.severe("Error closing socket: ${e.message}")
        }
        LOGGER.info("Disconnected from server.")
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger(ClientSocketHandler::class.java.getName())
    }
}