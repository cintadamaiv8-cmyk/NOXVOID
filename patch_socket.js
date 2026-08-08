const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/network/SocketClient.kt', 'utf8');

code = code.replace(
`data class GroupInfo(
    val description: String = "Selamat datang di Clash Of Clans Community.\\n\\nTempat berdiskusi strategi, war, dan rekrutmen klan secara private dan eksklusif. Patuhi aturan dan jaga kesopanan sesama anggota NOXVOID.",
    val banner: String = "",
    val avatar: String = ""
)`,
`data class GroupInfo(
    val name: String = "Clash Of Clans Community",
    val description: String = "Selamat datang di Clash Of Clans Community.\\n\\nTempat berdiskusi strategi, war, dan rekrutmen klan secara private dan eksklusif. Patuhi aturan dan jaga kesopanan sesama anggota NOXVOID.",
    val banner: String = "",
    val avatar: String = ""
)`);

code = code.replace(
`                        "group_info" -> {
                            _groupInfo.value = GroupInfo(
                                description = json.optString("description", _groupInfo.value.description),
                                banner = json.optString("banner", _groupInfo.value.banner),
                                avatar = json.optString("avatar", _groupInfo.value.avatar)
                            )
                        }`,
`                        "group_info" -> {
                            _groupInfo.value = GroupInfo(
                                name = json.optString("name", _groupInfo.value.name),
                                description = json.optString("description", _groupInfo.value.description),
                                banner = json.optString("banner", _groupInfo.value.banner),
                                avatar = json.optString("avatar", _groupInfo.value.avatar)
                            )
                        }`);

fs.writeFileSync('app/src/main/java/com/example/network/SocketClient.kt', code);
console.log("Patched SocketClient");
