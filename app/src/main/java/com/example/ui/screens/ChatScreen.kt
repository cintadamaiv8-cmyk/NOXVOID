package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.data.GroupRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.network.ChatMessage
import com.example.network.SocketClient
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    socketClient: SocketClient,
    currentUserNama: String,
    onBack: () -> Unit,
    onNavigateToGroupProfile: () -> Unit
) {
    val context = LocalContext.current
    val groupRepository = remember { GroupRepository(context) }
    val groupName by groupRepository.nameFlow.collectAsState(initial = "Clash Of Clans Community")
    val messages by socketClient.messages.collectAsState()
    val onlineUsers by socketClient.onlineUsersCount.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll logic
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            // If user is at the bottom, auto-scroll to the new message
            if (lastVisibleItemIndex == null || lastVisibleItemIndex >= messages.size - 3) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToGroupProfile() }
                                .padding(end = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.login_logo),
                                contentDescription = "Group Icon",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, BorderDark, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(groupName, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(OnlineGreen))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("$onlineUsers Member Online", color = TextGray, fontSize = 12.sp)
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
                )
                HorizontalDivider(color = BorderDark, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = BorderDark, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundDark)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ketik pesan...", color = TextGray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            cursorColor = AccentCyan,
                            focusedContainerColor = CardDark,
                            unfocusedContainerColor = CardDark
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                socketClient.sendMessage(inputText.trim())
                                inputText = ""
                                coroutineScope.launch {
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(AccentPurple, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = TextWhite, modifier = Modifier.size(24.dp))
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.nama == currentUserNama
                MessageBubble(msg, isMe)
            }
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage, isMe: Boolean) {
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = formatter.format(Date(msg.timestamp))
    
    val roleColor = when(msg.role.lowercase()) {
        "owner" -> AccentCyan
        "admin" -> AccentPurple
        else -> TextGray
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(msg.nama, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (msg.tag != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(msg.tag, color = TextGray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(roleColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(msg.role.uppercase(), color = roleColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isMe) {
                Text(timeString, color = TextGray, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp, end = 8.dp))
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .wrapContentWidth(if (isMe) Alignment.End else Alignment.Start)
                    .background(
                        color = if (isMe) AccentPurple else CardDark,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isMe) 20.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 20.dp
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (isMe) AccentPurple else BorderDark,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isMe) 20.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 20.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(msg.content, color = TextWhite, fontSize = 15.sp, lineHeight = 22.sp)
            }

            if (!isMe) {
                Text(timeString, color = TextGray, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp, start = 8.dp))
            }
        }
    }
}
