package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import com.example.data.GroupRepository
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
import com.example.MonitoringViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authRepository: AuthRepository,
    socketClient: SocketClient,
    monitoringViewModel: MonitoringViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val groupRepository = remember { GroupRepository(context) }
    val groupName by groupRepository.nameFlow.collectAsState(initial = "Clash Of Clans Community")
    val coroutineScope = rememberCoroutineScope()
    val isMonitoring by monitoringViewModel.isMonitoring.collectAsState()
    var pingStatus by remember { mutableStateOf("Offline") }
    var pingMs by remember { mutableStateOf("0 ms") }
    
    val onlineUsers by socketClient.onlineUsersCount.collectAsState()
    val isSocketConnected by socketClient.isConnected.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    LaunchedEffect(isMonitoring) {
        while (isMonitoring) {
            val start = System.currentTimeMillis()
            try {
                pingStatus = "Connecting..."
                val response = RetrofitClient.instance.ping()
                val elapsed = System.currentTimeMillis() - start
                if (response.status == "online") {
                    pingStatus = "Online"
                    pingMs = "${elapsed} ms"
                } else {
                    pingStatus = "Offline"
                }
            } catch (e: Exception) {
                pingStatus = "Offline"
                pingMs = "- ms"
            }
            delay(3000)
        }
        if (!isMonitoring) {
            pingStatus = "Offline"
            pingMs = "0 ms"
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(BackgroundDark)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.login_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, BorderDark, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("NOXVOID", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
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
                                coroutineScope.launch {
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
                .padding(20.dp)
        ) {
            // Banner
            Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.home_banner),
                        contentDescription = "Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, BackgroundDark.copy(alpha = 0.9f)),
                                    startY = 200f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Box(modifier = Modifier.background(AccentCyan, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("PRIVATE COMMUNITY", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Welcome to NOXVOID", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Community Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToChat() }
                    .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(groupName, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Internal private discussion", color = TextGray, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AccentPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(32.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
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
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (isSocketConnected) OnlineGreen else OfflineRed))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("$onlineUsers Members Online", color = TextWhite, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Enter", tint = TextWhite)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Server Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SERVER MONITORING", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Switch(
                            checked = isMonitoring,
                            onCheckedChange = { monitoringViewModel.setMonitoring(it) },
                            modifier = Modifier.height(24.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = AccentCyan,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = BorderDark
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Status Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BackgroundDark)
                                .padding(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("STATUS", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                val statusColor = when (pingStatus) {
                                    "Online" -> OnlineGreen
                                    "Connecting..." -> AccentCyan
                                    else -> OfflineRed
                                }
                                Text(pingStatus.uppercase(), color = statusColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        // Latency Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BackgroundDark)
                                .padding(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("LATENCY", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                val pingValue = pingMs.replace(" ms", "")
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(pingValue, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ms", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 2.dp))
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
