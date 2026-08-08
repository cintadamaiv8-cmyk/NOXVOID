const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/screens/GroupProfileScreen.kt', 'utf8');

// Update GroupProfileScreen to get onlineUsersList
code = code.replace("val onlineUsers by socketClient.onlineUsersCount.collectAsState()", 
`val onlineUsers by socketClient.onlineUsersCount.collectAsState()
    val onlineUsersList by socketClient.onlineUsersList.collectAsState()`);

// Update OnlineMembersBottomSheet call
code = code.replace("OnlineMembersBottomSheet(\n                onlineCount = onlineUsers,\n                onDismiss = { showOnlineList = false }\n            )", 
`OnlineMembersBottomSheet(
                onlineUsersList = onlineUsersList,
                onDismiss = { showOnlineList = false }
            )`);

// Update OnlineMembersBottomSheet definition
code = code.replace("@Composable\nfun OnlineMembersBottomSheet(onlineCount: Int, onDismiss: () -> Unit) {", 
`@Composable
fun OnlineMembersBottomSheet(onlineUsersList: List<SocketClient.OnlineUser>, onDismiss: () -> Unit) {`);

code = code.replace("Anggota Online ($onlineCount)", "Anggota Online (\\${onlineUsersList.size})");

code = code.replace("items(onlineCount) { index ->", "items(onlineUsersList) { user ->");

// Update list item
code = code.replace(
`                                Text("Online Member \\${index + 1}", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(8.dp))
                                if (index == 0) {
                                    Box(
                                        modifier = Modifier
                                            .background(AccentCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("OWNER", color = AccentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(TextGray.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("MEMBER", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                    }
                                }`.replace(/\\/g, ''),
`                                Text(user.nama, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
                                }`
);


fs.writeFileSync('app/src/main/java/com/example/ui/screens/GroupProfileScreen.kt', code);
console.log("Patched UI");
