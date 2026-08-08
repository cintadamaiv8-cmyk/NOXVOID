package com.example.network

import android.util.Log
import com.example.data.AppConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

data class ChatMessage(
    val id: Int = 0,
    val nama: String,
    val role: String,
    val tag: String? = null,
    val content: String,
    val timestamp: Long
)

data class GroupInfo(
    val name: String = "Clash Of Clans Community",
    val description: String = "Selamat datang di Clash Of Clans Community.\n\nTempat berdiskusi strategi, war, dan rekrutmen klan secara private dan eksklusif. Patuhi aturan dan jaga kesopanan sesama anggota NOXVOID.",
    val banner: String = "",
    val avatar: String = ""
)

class SocketClient(private val token: String) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    private val _onlineUsersCount = MutableStateFlow(0)
    val onlineUsersCount: StateFlow<Int> = _onlineUsersCount
    
    data class OnlineUser(val nama: String, val role: String, val tag: String?)
    private val _onlineUsersList = MutableStateFlow<List<OnlineUser>>(emptyList())
    val onlineUsersList: StateFlow<List<OnlineUser>> = _onlineUsersList

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _groupInfo = MutableStateFlow(GroupInfo())
    val groupInfo: StateFlow<GroupInfo> = _groupInfo

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastMessage: SharedFlow<String> = _toastMessage

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
                            if (json.has("users")) {
                                val usersArray = json.getJSONArray("users")
                                val usersList = mutableListOf<OnlineUser>()
                                for (i in 0 until usersArray.length()) {
                                    val u = usersArray.getJSONObject(i)
                                    usersList.add(OnlineUser(
                                        nama = u.optString("nama"),
                                        role = u.optString("role"),
                                        tag = if (u.has("tag") && !u.isNull("tag")) u.getString("tag") else null
                                    ))
                                }
                                _onlineUsersList.value = usersList
                            }
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
                        "group_info" -> {
                            _groupInfo.value = GroupInfo(
                                name = json.optString("name", _groupInfo.value.name),
                                description = json.optString("description", _groupInfo.value.description),
                                banner = json.optString("banner", _groupInfo.value.banner),
                                avatar = json.optString("avatar", _groupInfo.value.avatar)
                            )
                        }
                        "toast" -> {
                            val message = json.optString("message", "")
                            if (message.isNotEmpty()) {
                                _toastMessage.tryEmit(message)
                            }
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
            tag = if (obj.has("tag") && !obj.isNull("tag")) obj.getString("tag") else null,
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

    fun updateGroupProfile(field: String, value: String) {
        if (_isConnected.value) {
            val msg = JSONObject().apply {
                put("type", "update_group")
                put("field", field)
                put("value", value)
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
