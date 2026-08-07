package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AuthRepository
import com.example.network.RetrofitClient
import com.example.network.SocketClient
import com.example.data.MonitoringManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authRepository: AuthRepository,
    socketClient: SocketClient,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    val onlineUsers by socketClient.onlineUsersCount.collectAsState()
    val isSocketConnected by socketClient.isConnected.collectAsState()

    // Monitoring state moved to MonitoringManager to persist across screens
    val isMonitoring by MonitoringManager.isMonitoring.collectAsState()
    val pingStatus by MonitoringManager.pingStatus.collectAsState()
    val pingMs by MonitoringManager.pingMs.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(BackgroundDark)
                    .border(1.dp, BorderDark)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_login),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("NOXVOID", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("Private Community", color = TextGray, fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, BorderDark, CircleShape)
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextWhite)
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(CardDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profil", color = TextWhite) },
                            onClick = {
                                showMenu = false
                                onNavigateToProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tentang", color = TextWhite) },
                            onClick = {
                                showMenu = false
                                showAbout = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout", color = OfflineRed) },
                            onClick = {
                                showMenu = false
                                kotlinx.coroutines.MainScope().launch {
                                    authRepository.clearAuthData()
                                    socketClient.disconnect()
                                    onLogout()
                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp)
        ) {
            // Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                    .padding(6.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner_home),
                    contentDescription = "Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).background(AccentCyan, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("PRIVATE COMMUNITY", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Community Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToChat() }
                    .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text("Clash Of Clans Community", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Internal private discussion", color = TextGray, fontSize = 13.sp, fontStyle = FontStyle.Italic)
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BorderDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = AccentCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onNavigateToChat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (isSocketConnected) OnlineGreen else OfflineRed))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("$onlineUsers Members Online", color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Enter", tint = TextWhite)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Server Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SERVER STATISTICS", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BackgroundDark)
                                .border(1.dp, BorderDark, RoundedCornerShape(50))
                                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("MONITORING", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isMonitoring,
                                onCheckedChange = { MonitoringManager.setMonitoring(it) },
                                modifier = Modifier.height(24.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextWhite,
                                    checkedTrackColor = AccentCyan,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = BorderDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BackgroundDark)
                                .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text("STATUS", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val statusColor = when (pingStatus) {
                                        "Online" -> OnlineGreen
                                        "Connecting..." -> AccentCyan
                                        else -> OfflineRed
                                    }
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(pingStatus, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BackgroundDark)
                                .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text("LATENCY", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    val pingValue = pingMs.replace(" ms", "")
                                    Text(pingValue, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ms", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAbout) {
            AlertDialog(
                onDismissRequest = { showAbout = false },
                containerColor = CardDark,
                title = { Text("Tentang NoxVoid", color = TextWhite) },
                text = { Text("Private Community Chat App\nVersion 1.0", color = TextGray) },
                confirmButton = {
                    TextButton(onClick = { showAbout = false }) {
                        Text("Tutup", color = AccentCyan)
                    }
                }
            )
        }
    }
}
