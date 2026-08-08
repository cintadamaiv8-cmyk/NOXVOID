const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/MainActivity.kt', 'utf8');

if(!code.includes('import com.example.data.GroupRepository')) {
    code = code.replace("import com.example.data.AuthRepository", "import com.example.data.AuthRepository\nimport com.example.data.GroupRepository");
}

code = code.replace("authRepository = AuthRepository(applicationContext)", 
`authRepository = AuthRepository(applicationContext)
        val groupRepository = GroupRepository(applicationContext)`);

code = code.replace(
`                        socketClient = SocketClient(savedToken)
                        socketClient?.connect()
                        startDestination = "home"`,
`                        socketClient = SocketClient(savedToken)
                        socketClient?.connect()
                        startDestination = "home"
                        
                        kotlinx.coroutines.GlobalScope.launch {
                            socketClient?.groupInfo?.collect { info ->
                                if (info.name.isNotEmpty()) {
                                    groupRepository.saveGroupData(info.name, info.description, info.banner, info.avatar)
                                }
                            }
                        }`);

code = code.replace(
`                                        socketClient = SocketClient(t)
                                        socketClient?.connect()`,
`                                        socketClient = SocketClient(t)
                                        socketClient?.connect()
                                        
                                        kotlinx.coroutines.GlobalScope.launch {
                                            socketClient?.groupInfo?.collect { info ->
                                                if (info.name.isNotEmpty()) {
                                                    groupRepository.saveGroupData(info.name, info.description, info.banner, info.avatar)
                                                }
                                            }
                                        }`);
fs.writeFileSync('app/src/main/java/com/example/MainActivity.kt', code);
console.log("Patched MainActivity");
