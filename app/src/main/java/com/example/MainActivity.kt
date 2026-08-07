package com.example
import kotlinx.coroutines.launch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AuthRepository
import com.example.network.SocketClient
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NoxVoidTheme
import kotlinx.coroutines.flow.firstOrNull

class MainActivity : ComponentActivity() {
    private lateinit var authRepository: AuthRepository
    private var socketClient: SocketClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authRepository = AuthRepository(applicationContext)

        setContent {
            NoxVoidTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }
                var token by remember { mutableStateOf<String?>(null) }
                var currentUserNama by remember { mutableStateOf<String>("") }

                LaunchedEffect(Unit) {
                    val savedToken = authRepository.tokenFlow.firstOrNull()
                    val savedNama = authRepository.namaFlow.firstOrNull()
                    if (savedToken != null && savedNama != null) {
                        token = savedToken
                        currentUserNama = savedNama
                        socketClient = SocketClient(savedToken)
                        socketClient?.connect()
                        startDestination = "home"
                    } else {
                        startDestination = "login"
                    }
                }

                if (startDestination == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    NavHost(navController = navController, startDestination = startDestination!!) {
                        composable("login") {
                            LoginScreen(
                                authRepository = authRepository,
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            // Ensure socket is connected if arrived from login
                            LaunchedEffect(Unit) {
                                if (socketClient == null) {
                                    val t = authRepository.tokenFlow.firstOrNull()
                                    val n = authRepository.namaFlow.firstOrNull()
                                    if (t != null && n != null) {
                                        token = t
                                        currentUserNama = n
                                        socketClient = SocketClient(t)
                                        socketClient?.connect()
                                    }
                                }
                            }
                            HomeScreen(
                                authRepository = authRepository,
                                socketClient = socketClient ?: SocketClient(""),
                                onNavigateToChat = { navController.navigate("chat") },
                                onNavigateToProfile = { navController.navigate("profile") },
                                onLogout = {
                                    socketClient = null; kotlinx.coroutines.MainScope().launch { authRepository.clearAuthData() }
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("chat") {
                            ChatScreen(
                                socketClient = socketClient ?: SocketClient(""),
                                currentUserNama = currentUserNama,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                authRepository = authRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socketClient?.disconnect()
    }
}
