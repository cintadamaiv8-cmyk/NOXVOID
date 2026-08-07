package com.example.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.network.RetrofitClient

object MonitoringManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _pingStatus = MutableStateFlow("Offline")
    val pingStatus: StateFlow<String> = _pingStatus.asStateFlow()

    private val _pingMs = MutableStateFlow("0 ms")
    val pingMs: StateFlow<String> = _pingMs.asStateFlow()

    private var loopJob: Job? = null

    fun setMonitoring(enabled: Boolean) {
        if (_isMonitoring.value == enabled) return
        _isMonitoring.value = enabled
        if (enabled) startLoop() else stopLoop()
    }

    private fun startLoop() {
        loopJob = scope.launch {
            while (_isMonitoring.value) {
                val start = System.currentTimeMillis()
                try {
                    _pingStatus.value = "Connecting..."
                    val response = RetrofitClient.instance.ping()
                    val elapsed = System.currentTimeMillis() - start
                    if (response.status == "online") {
                        _pingStatus.value = "Online"
                        _pingMs.value = "${elapsed} ms"
                    } else {
                        _pingStatus.value = "Offline"
                        _pingMs.value = "0 ms"
                    }
                } catch (e: Exception) {
                    _pingStatus.value = "Offline"
                    _pingMs.value = "- ms"
                }
                delay(3000)
            }
            // ensure reset when stopped
            if (!_isMonitoring.value) {
                _pingStatus.value = "Offline"
                _pingMs.value = "0 ms"
            }
        }
    }

    private fun stopLoop() {
        loopJob?.cancel()
        loopJob = null
        _pingStatus.value = "Offline"
        _pingMs.value = "0 ms"
    }
}
