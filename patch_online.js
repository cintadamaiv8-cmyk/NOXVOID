const fs = require('fs');
let code = fs.readFileSync('backend/server.js', 'utf8');

// Change onlineUsers to Map
code = code.replace("let onlineUsers = new Set();", "let onlineUsers = new Map();");

code = code.replace(
"function broadcastOnlineCount() {", 
`function broadcastOnlineCount() {
    broadcast({ type: 'online_users', count: onlineUsers.size, users: Array.from(onlineUsers.values()) });
}`);

// We need to remove the old broadcastOnlineCount which has the count size
code = code.replace("broadcast({ type: 'online_users', count: onlineUsers.size });", "");

code = code.replace("onlineUsers.add(ws.user.nama);", "onlineUsers.set(ws.user.nama, { nama: ws.user.nama, role: ws.user.role, tag: ws.user.tag });");

code = code.replace("onlineUsers.delete(ws.user.nama);", "onlineUsers.delete(ws.user.nama);");

fs.writeFileSync('backend/server.js', code);
console.log("Patched online users");
