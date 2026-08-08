const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/screens/ChatScreen.kt', 'utf8');

if(!code.includes('import androidx.compose.ui.platform.LocalContext')) {
    code = code.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.compose.ui.platform.LocalContext\nimport com.example.data.GroupRepository");
}

code = code.replace(
`fun ChatScreen(
    socketClient: SocketClient,
    currentUserNama: String,
    onBack: () -> Unit,
    onNavigateToGroupProfile: () -> Unit
) {`,
`fun ChatScreen(
    socketClient: SocketClient,
    currentUserNama: String,
    onBack: () -> Unit,
    onNavigateToGroupProfile: () -> Unit
) {
    val context = LocalContext.current
    val groupRepository = remember { GroupRepository(context) }
    val groupName by groupRepository.nameFlow.collectAsState(initial = "Clash Of Clans Community")`);

code = code.replace(`Text("Clash Of Clans Community", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)`,
`Text(groupName, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)`);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/ChatScreen.kt', code);
console.log("Patched ChatScreen");
