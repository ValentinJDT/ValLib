package fr.valentin.lib.vallib.tcp.connection

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class ServerSocketHandler(private val port: Int) {
    private val clientMap = ConcurrentHashMap<UUID, Socket>()
    private var serverSocket: ServerSocket? = null
    @Volatile
    private var isRunning: Boolean = true

    fun start() {
        serverSocket = ServerSocket(port)
        println("Server started on port $port")

        while (isRunning) {
            val clientSocket = serverSocket!!.accept()
            val clientUUID = addClient(clientSocket)

            thread { handleClient(clientUUID, clientSocket) }
        }
    }

    fun stopServer() {
        isRunning = false
        clientMap.keys.forEach { kickClient(it) }
        serverSocket?.close()
        println("Server stopped.")
    }

    private fun addClient(clientSocket: Socket): UUID {
        val clientUUID = UUID.randomUUID()
        clientMap[clientUUID] = clientSocket
        sendToClient(clientUUID, clientUUID.toString())
        println("Client connected: $clientUUID")
        return clientUUID
    }

    private fun handleClient(uuid: UUID, socket: Socket) {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

        try {
            reader.lineSequence().forEach { processMessage(uuid, socket, it) }
        } catch (e: IOException) {
            println("Client $uuid disconnected.")
        } finally {
            removeClient(uuid, socket)
        }
    }

    private fun processMessage(uuid: UUID, socket: Socket, message: String) {
        when {
            message.startsWith("TO") -> {
                val targetUUIDMessage = message.substringAfter("TO").trim()
                val targetUUID = targetUUIDMessage.substringBefore(":").trim()
                val actualMessage = targetUUIDMessage.substringAfter(":").trim()

                try {
                    val targetUUIDParsed = UUID.fromString(targetUUID)
                    sendToClient(targetUUIDParsed, "FROM $uuid: $actualMessage")
                } catch (e: IllegalArgumentException) {
                    println("Invalid UUID format from client $uuid.")
                    sendToClient(uuid, "Invalid UUID format.")
                }
            }

            message.startsWith("GLOBAL") -> {
                val actualMessage = message.substringAfter("GLOBAL:").trim()
                broadcastMessage(uuid, actualMessage)
            }

            message == "DISCONNECT" -> {
                println("Client $uuid requested disconnection.")
                removeClient(uuid, socket)
            }

            else -> {
                println("Received from $uuid: $message")
            }
        }
    }

    private fun removeClient(uuid: UUID, socket: Socket) {
        clientMap.remove(uuid)
        socket.close()
    }

    fun sendToClient(uuid: UUID, message: String) {
        clientMap[uuid]?.let {
            PrintWriter(it.getOutputStream(), true).println(message)
        } ?: println("Client $uuid not found or disconnected.")
    }

    private fun broadcastMessage(senderUUID: UUID, message: String) {
        clientMap.keys.forEach { targetUUID ->
            if (targetUUID != senderUUID) {
                sendToClient(targetUUID, "GLOBAL $senderUUID: $message")
            }
        }
    }

    fun kickClient(uuid: UUID) {
        val clientSocket = clientMap[uuid]
        if (clientSocket != null) {
            sendToClient(uuid, "KICK")
            clientMap.remove(uuid)
            clientSocket.close()
            println("Client $uuid has been kicked.")
        } else {
            println("Client $uuid not found or already disconnected.")
        }
    }
}

class ClientSocketHandler(
    private val serverIp: String,
    private val serverPort: Int,
    private val maxRetries: Int = 3,
    private val retryTimeout: Long = 1000L
) {
    private var clientUUID: UUID? = null
    private var socket: Socket? = null

    val uuid
        get() = clientUUID

    fun connect() {
        var attempts = 0
        while (attempts < maxRetries) {
            if (tryConnecting()) return
            attempts++
            handleConnectionFailure(attempts)
        }
    }

    private fun tryConnecting(): Boolean {
        return try {
            socket = Socket(serverIp, serverPort)
            val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            clientUUID = UUID.fromString(reader.readLine())
            println("Connected to server with UUID: $clientUUID")
            thread { listenToServer() }
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun handleConnectionFailure(attempts: Int) {
        println("Connection failed. Attempt $attempts/$maxRetries.")
        if (attempts < maxRetries) {
            Thread.sleep(retryTimeout)
        } else {
            println("Failed to connect after $maxRetries attempts.")
        }
    }

    private fun listenToServer() {
        val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
        reader.lineSequence().forEach { handleMessage(it) }
    }

    private fun handleMessage(message: String) {
        if (message == "KICK") {
            println("Kicked from server.")
            socket!!.close()
            return
        }

        println(message)
    }

    fun sendMessage(message: String) {
        createWriter().println(message)
    }

    fun sendMessageToClient(targetUUID: UUID, message: String) {
        sendMessage("TO $targetUUID: $message")
    }

    fun sendBroadcastMessage(message: String) {
        sendMessage("GLOBAL: $message")
    }

    private fun createWriter(): PrintWriter {
        return PrintWriter(socket!!.getOutputStream(), true)
    }

    fun disconnect() {
        sendMessage("DISCONNECT")
        socket!!.close()
        println("Disconnected from server.")
    }
}