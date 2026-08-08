import os

with open('app/src/main/java/com/example/ui/screens/GroupProfileScreen.kt', 'r') as f:
    code = f.read()

if 'import com.example.data.GroupRepository' not in code:
    code = code.replace("import com.example.data.AuthRepository", "import com.example.data.AuthRepository\nimport com.example.data.GroupRepository\nimport androidx.activity.compose.rememberLauncherForActivityResult\nimport androidx.activity.result.contract.ActivityResultContracts\nimport android.net.Uri\nimport java.io.File\nimport java.io.FileOutputStream\nimport android.content.Context\nimport coil.compose.AsyncImage\nimport androidx.compose.material.icons.filled.Edit")

code = code.replace(
"""fun GroupProfileScreen(
    socketClient: SocketClient,
    authRepository: AuthRepository,
    onBack: () -> Unit
) {""",
"""fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupProfileScreen(
    socketClient: SocketClient,
    authRepository: AuthRepository,
    onBack: () -> Unit
) {""")

code = code.replace(
"""    val groupInfo by socketClient.groupInfo.collectAsState()""",
"""    val context = LocalContext.current
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
    }""")

code = code.replace("val context = LocalContext.current\n\n    LaunchedEffect", "    LaunchedEffect")

code = code.replace(
"""                    Image(
                        painter = painterResource(id = R.drawable.home_banner),
                        contentDescription = "Cover Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { socketClient.updateGroupProfile("banner", "new_banner") },
                        contentScale = ContentScale.Crop
                    )""",
"""                    if (groupBanner.isNotEmpty() && File(groupBanner).exists()) {
                        AsyncImage(
                            model = groupBanner,
                            contentDescription = "Cover Banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable { bannerLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.home_banner),
                            contentDescription = "Cover Banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable { bannerLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    }""")

code = code.replace(
"""                        Image(
                            painter = painterResource(id = R.drawable.login_logo),
                            contentDescription = "Group Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(4.dp, BackgroundDark, CircleShape)
                                .clickable { socketClient.updateGroupProfile("avatar", "new_avatar") },
                            contentScale = ContentScale.Crop
                        )""",
"""                        if (groupAvatar.isNotEmpty() && File(groupAvatar).exists()) {
                            AsyncImage(
                                model = groupAvatar,
                                contentDescription = "Group Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, BackgroundDark, CircleShape)
                                    .clickable { avatarLauncher.launch("image/*") },
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
                                    .clickable { avatarLauncher.launch("image/*") },
                                contentScale = ContentScale.Crop
                            )
                        }""")

code = code.replace(
"""                    Text(
                        text = "Clash Of Clans Community",
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )""",
"""                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                        editNameText = groupName
                        showEditName = true
                    }) {
                        Text(
                            text = groupName,
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = TextGray, modifier = Modifier.size(16.dp))
                    }""")

code = code.replace(
"""                                editDescriptionText = groupInfo.description
                                showEditDescription = true""",
"""                                editDescriptionText = groupDesc
                                showEditDescription = true""")

code = code.replace(
"""                        Text(
                            text = groupInfo.description,
                            color = TextWhite,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(16.dp)
                        )""",
"""                        Text(
                            text = groupDesc,
                            color = TextWhite,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(16.dp)
                        )""")

if 'showEditName) {' not in code:
    code = code.replace(
"""        if (showEditDescription) {
            AlertDialog(""",
"""        if (showEditName) {
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
            AlertDialog(""")


with open('app/src/main/java/com/example/ui/screens/GroupProfileScreen.kt', 'w') as f:
    f.write(code)

print("Patched GroupProfileScreen via Python")
