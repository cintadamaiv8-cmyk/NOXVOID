const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf8');

if(!code.includes('import com.example.data.GroupRepository')) {
    code = code.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport com.example.data.GroupRepository");
}

code = code.replace(
`fun HomeScreen(
    authRepository: AuthRepository,
    socketClient: SocketClient,
    monitoringViewModel: MonitoringViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {`,
`fun HomeScreen(
    authRepository: AuthRepository,
    socketClient: SocketClient,
    monitoringViewModel: MonitoringViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val groupRepository = remember { GroupRepository(context) }
    val groupName by groupRepository.nameFlow.collectAsState(initial = "Clash Of Clans Community")`);

code = code.replace(`Text("Clash Of Clans Community", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)`,
`Text(groupName, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)`);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', code);
console.log("Patched HomeScreen");
