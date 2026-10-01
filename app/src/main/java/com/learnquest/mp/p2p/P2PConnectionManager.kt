package com.learnquest.mp.p2p

import android.util.Log
import com.learnquest.mp.p2p.models.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import org.json.JSONObject

class P2PConnectionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var serverSocket: ServerSocket? = null
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null

    private val _incomingMessages = MutableStateFlow<BattleMessage?>(null)
    val incomingMessages: StateFlow<BattleMessage?> = _incomingMessages.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun startHostServer(port: Int = 8888, onClientConnected: () -> Unit) {
        scope.launch {
            try {
                serverSocket?.close()
                serverSocket = ServerSocket(port)
                Log.d("P2PConnection", "Server socket listening on port $port")
                val clientSocket = serverSocket!!.accept()
                socket = clientSocket
                setupStreams(clientSocket)
                _isConnected.value = true
                withContext(Dispatchers.Main) {
                    onClientConnected()
                }
                listenForMessages()
            } catch (e: Exception) {
                Log.e("P2PConnection", "Host server error", e)
                _isConnected.value = false
            }
        }
    }

    fun connectToHost(hostAddress: InetAddress, port: Int = 8888, onConnected: () -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                socket?.close()
                val clientSocket = Socket(hostAddress, port)
                socket = clientSocket
                setupStreams(clientSocket)
                _isConnected.value = true
                withContext(Dispatchers.Main) {
                    onConnected()
                }
                listenForMessages()
            } catch (e: Exception) {
                Log.e("P2PConnection", "Client connect error", e)
                _isConnected.value = false
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to connect to host")
                }
            }
        }
    }

    private fun setupStreams(s: Socket) {
        writer = PrintWriter(s.getOutputStream(), true)
        reader = BufferedReader(InputStreamReader(s.getInputStream()))
    }

    private fun listenForMessages() {
        scope.launch {
            try {
                var line: String? = null
                while (reader != null && reader!!.readLine().also { line = it } != null) {
                    line?.let { raw ->
                        val msg = parseMessage(raw)
                        if (msg != null) {
                            _incomingMessages.value = msg
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("P2PConnection", "Read loop error", e)
            } finally {
                _isConnected.value = false
            }
        }
    }

    fun sendMessage(msg: BattleMessage) {
        scope.launch {
            try {
                val jsonStr = serializeMessage(msg)
                writer?.println(jsonStr)
            } catch (e: Exception) {
                Log.e("P2PConnection", "Send message error", e)
            }
        }
    }

    fun disconnect() {
        try {
            writer?.close()
            reader?.close()
            socket?.close()
            serverSocket?.close()
        } catch (_: Exception) {}
        _isConnected.value = false
        _incomingMessages.value = null
    }

    private fun serializeMessage(msg: BattleMessage): String {
        val json = JSONObject()
        json.put("messageId", msg.messageId)
        json.put("battleId", msg.battleId)
        json.put("type", msg.type.name)
        json.put("senderId", msg.senderId)
        json.put("timestamp", msg.timestamp)
        json.put("payloadJson", msg.payloadJson)
        return json.toString()
    }

    private fun parseMessage(raw: String): BattleMessage? {
        return try {
            val json = JSONObject(raw)
            BattleMessage(
                messageId = json.optString("messageId"),
                battleId = json.optString("battleId"),
                type = MessageType.valueOf(json.optString("type")),
                senderId = json.optString("senderId"),
                timestamp = json.optLong("timestamp"),
                payloadJson = json.optString("payloadJson")
            )
        } catch (e: Exception) {
            Log.e("P2PConnection", "JSON parse error", e)
            null
        }
    }

    fun close() {
        disconnect()
        scope.cancel()
    }
}
