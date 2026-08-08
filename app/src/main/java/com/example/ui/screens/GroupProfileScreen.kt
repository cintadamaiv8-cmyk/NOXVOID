package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AuthRepository
import com.example.data.GroupRepository
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Edit
import com.example.network.SocketClient
import com.example.ui.theme.*
import kotlinx.coroutines.flow.firstOrNull

fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}



fun canEditGroup(role: String, field: String): Boolean {
    val r = role.lowercase()
    return when(field) {
        "banner", "name" -> r == "owner"
        "avatar", "description" -> r == "owner" || r == "admin"
        else -> false
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupProfileScreen(
    socketClient: SocketClient,
    authRepository: AuthRepository,
    onBack: () -> Unit
) {
    val onlineUsers by socketClient.onlineUsersCount.collectAsState()
    val onlineUsersList by socketClient.onlineUsersList.collectAsState()
    val context = LocalContext.current
    val groupRepository = remember { GroupRepository(context) }
    val groupName by groupRepository.nameFlow.collectAsState(initial = "Clash Of Clans Community")
    val groupDesc by groupRepository.descFlow.collectAsState(initial = "")
    val groupBanner by groupRepository.bannerFlow.collectAsState(initial = "")
    val groupAvatar by groupRepository.avatarFlow.collectAsState(initial = "")

    var showEditName by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    
    val bannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val path = copyUriToInternalStorage(context, uri, "banner_${System.currentTimeMillis()}.jpg")
            if (path != null) {
                socketClient.updateGroupProfile("banner", path)
            }
        }
    }
    
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val path = copyUriToInternalStorage(context, uri, "avatar_${System.currentTimeMillis()}.jpg")
            if (path != null) {
                socketClient.updateGroupProfile("avatar", path)
            }
        }
    }
    var showOnlineList by remember { mutableStateOf(false) }
    var currentUserRole by remember { mutableStateOf("") }
    
    var showEditDescription by remember { mutableStateOf(false) }
    var editDescriptionText by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
        val role = authRepository.roleFlow.firstOrNull()
        if (role != null) currentUserRole = role
    }

    LaunchedEffect(Unit) {
        socketClient.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Header (Banner & Avatar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp) // Total height for the header area
                ) {
                    // Banner
                    if (groupBanner.isNotEmpty() && File(groupBanner).exists()) {
                        AsyncImage(
                            model = groupBanner,
                            contentDescription = "Cover Banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable { 
                                    if (canEditGroup(currentUserRole, "banner")) bannerLauncher.launch("image/*")
                                    else Toast.makeText(context, "Anda bukan admin/owner", Toast.LENGTH_SHORT).show()
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.home_banner),
                            contentDescription = "Cover Banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable { 
                                    if (canEditGroup(currentUserRole, "banner")) bannerLauncher.launch("image/*")
                                    else Toast.makeText(context, "Anda bukan admin/owner", Toast.LENGTH_SHORT).show()
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Gradient overlay on banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.3f), BackgroundDark),
                                    startY = 50f
                                )
                            )
                    )

                    // Top Bar (Back & Menu)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBack) {
                            Box(modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                            }
                        }
                        IconButton(onClick = { /* Menu */ }) {
                            Box(modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(8.dp)
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextWhite)
                            }
                        }
                    }

                    // Avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    ) {
                        if (groupAvatar.isNotEmpty() && File(groupAvatar).exists()) {
                            AsyncImage(
                                model = groupAvatar,
                                contentDescription = "Group Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, BackgroundDark, CircleShape)
                                    .clickable { 
                                    if (canEditGroup(currentUserRole, "avatar")) avatarLauncher.launch("image/*")
                                    else Toast.makeText(context, "Anda bukan admin/owner", Toast.LENGTH_SHORT).show()
                                },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.login_logo),
                                contentDescription = "Group Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, BackgroundDark, CircleShape)
                                    .clickable { 
                                    if (canEditGroup(currentUserRole, "avatar")) avatarLauncher.launch("image/*")
                                    else Toast.makeText(context, "Anda bukan admin/owner", Toast.LENGTH_SHORT).show()
                                },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Group Info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                        if (canEditGroup(currentUserRole, "name")) {
                            editNameText = groupName
                            showEditName = true
                        } else {
                            Toast.makeText(context, "Anda bukan admin/owner", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(
                            text = groupName,
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "128 anggota",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(OnlineGreen))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$onlineUsers orang sedang online",
                            color = AccentCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                // Description Section
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "Tentang",
                        color = AccentCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                            .clickable {
                                if (canEditGroup(currentUserRole, "description")) {
                                    editDescriptionText = groupDesc
                                    showEditDescription = true
                                } else {
                                    Toast.makeText(context, "Anda bukan admin/owner", Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Text(
                            text = groupDesc,
                            color = TextWhite,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Online Realtime Section
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "Online sekarang",
                        color = AccentCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showOnlineList = true }
                            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(OnlineGreen))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$onlineUsers orang sedang online",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Other Features
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    FeatureItem(icon = Icons.Default.Group, title = "Anggota", subtitle = "Lihat semua 128 anggota")
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureItem(icon = Icons.Default.Notifications, title = "Notifikasi", subtitle = "Bungkam atau ubah nada dering")
                    Spacer(modifier = Modifier.height(12.dp))
                    if (currentUserRole.lowercase() == "owner" || currentUserRole.lowercase() == "admin") {
                        FeatureItem(icon = Icons.Default.PersonAdd, title = "Undang anggota", subtitle = "Buat tautan undangan")
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    FeatureItem(icon = Icons.Default.Search, title = "Cari dalam chat", subtitle = "Temukan pesan lama")
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showOnlineList) {
            OnlineMembersBottomSheet(
                onlineUsersList = onlineUsersList,
                onDismiss = { showOnlineList = false }
            )
        }
        
        if (showEditName) {
            AlertDialog(
                onDismissRequest = { showEditName = false },
                containerColor = CardDark,
                title = { Text("Ubah Nama", color = TextWhite) },
                text = {
                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = { editNameText = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { 
                        socketClient.updateGroupProfile("name", editNameText)
                        showEditName = false 
                    }) {
                        Text("Simpan", color = AccentCyan)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditName = false }) {
                        Text("Batal", color = TextGray)
                    }
                }
            )
        }
        
        if (showEditDescription) {
            AlertDialog(
                onDismissRequest = { showEditDescription = false },
                containerColor = CardDark,
                title = { Text("Ubah Deskripsi", color = TextWhite) },
                text = {
                    OutlinedTextField(
                        value = editDescriptionText,
                        onValueChange = { editDescriptionText = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { 
                        socketClient.updateGroupProfile("description", editDescriptionText)
                        showEditDescription = false 
                    }) {
                        Text("Simpan", color = AccentCyan)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDescription = false }) {
                        Text("Batal", color = TextGray)
                    }
                }
            )
        }
    }
}


@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .clickable { }
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AccentPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = AccentPurple)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextGray, fontSize = 12.sp)
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineMembersBottomSheet(onlineUsersList: List<SocketClient.OnlineUser>, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderDark) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(OnlineGreen))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Anggota Online (${onlineUsersList.size})",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(onlineUsersList) { user ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BackgroundDark)
                                .border(1.dp, BorderDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = TextGray, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.nama, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                val roleColor = when(user.role.lowercase()) {
                                    "owner" -> AccentCyan
                                    "admin" -> AccentPurple
                                    else -> TextGray
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .background(roleColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(user.role.uppercase(), color = roleColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                }
                                
                                if (user.tag != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(user.tag, color = TextGray, fontSize = 12.sp)
                                }
                            }
                            Text("Sedang online", color = OnlineGreen, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
