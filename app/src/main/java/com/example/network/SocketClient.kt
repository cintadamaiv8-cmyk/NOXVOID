package com.example.network

import android.util.Log
import com.example.data.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

data class ChatMessage(
    val id: Int = 0,
    val nama: String,
    val role: String,
    val content: String,
    val timestamp: Long
)

class SocketClient(private val token: String) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    private val _onlineUsersCount = MutableStateFlow(0)
    val onlineUsersCount: StateFlow<Int> = _onlineUsersCount

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    fun connect() {
        val request = Request.Builder()
            .url(AppConfig.WS_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isConnected.value = true
                // Send auth token
                val authMsg = JSONObject().apply {
                    put("type", "auth")
                    put("token", token)
                }
                webSocket.send(authMsg.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.getString("type")) {
                        "online_users" -> {
                            _onlineUsersCount.value = json.getInt("count")
                        }
                        "history" -> {
                            val msgArray = json.getJSONArray("messages")
                            val history = mutableListOf<ChatMessage>()
                            for (i in 0 until msgArray.length()) {
                                val obj = msgArray.getJSONObject(i)
                                history.add(parseMessage(obj))
                            }
                            _messages.value = history
                        }
                        "receive_message" -> {
                            val msg = parseMessage(json.getJSONObject("message"))
                            _messages.value = _messages.value + msg
                        }
                        "error" -> {
                            Log.e("SocketClient", "Error: ${json.getString("message")}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SocketClient", "Message parse error", e)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
                Log.e("SocketClient", "Failure", t)
            }
        })
    }

    private fun parseMessage(obj: JSONObject): ChatMessage {
        return ChatMessage(
            id = obj.optInt("id", 0),
            nama = obj.optString("nama", "Unknown"),
            role = obj.optString("role", "member"),
            content = obj.optString("content", ""),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
        )
    }

    fun sendMessage(content: String) {
        if (_isConnected.value) {
            val msg = JSONObject().apply {
                put("type", "send_message")
                put("content", content)
            }
            webSocket?.send(msg.toString())
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _isConnected.value = false
    }
}
