const express = require('express');
const http = require('http');
const { WebSocketServer, WebSocket } = require('ws');
const jwt = require('jsonwebtoken');
const fs = require('fs');
const cors = require('cors');
const bcrypt = require('bcryptjs');

const app = express();
app.use(express.json());
app.use(cors());

const server = http.createServer(app);
const wss = new WebSocketServer({ server });

const JWT_SECRET = 'noxvoid_super_secret_key_123';
const PORT = 3000;

const messagesFile = './messages.json';

const groupFile = './group.json';
function getGroupInfo() {
    try {
        if (!fs.existsSync(groupFile)) {
            const initial = {
                name: "Clash Of Clans Community",
                description: "Selamat datang di Clash Of Clans Community.\n\nTempat berdiskusi strategi, war, dan rekrutmen klan secara private dan eksklusif. Patuhi aturan dan jaga kesopanan sesama anggota NOXVOID.",
                banner: "",
                avatar: ""
            };
            fs.writeFileSync(groupFile, JSON.stringify(initial, null, 2));
            return initial;
        }
        return JSON.parse(fs.readFileSync(groupFile));
    } catch (e) {
        return { description: "", banner: "", avatar: "" };
    }
}
function saveGroupInfo(info) {
    fs.writeFileSync(groupFile, JSON.stringify(info, null, 2));
}


function getMessages() {
    try {
        if (!fs.existsSync(messagesFile)) {
            fs.writeFileSync(messagesFile, '[]');
        }
        const data = fs.readFileSync(messagesFile);
        return JSON.parse(data);
    } catch (e) {
        console.error("Error reading messages.json", e);
        return [];
    }
}

function saveMessage(msg) {
    try {
        const messages = getMessages();
        msg.id = messages.length > 0 ? messages[messages.length - 1].id + 1 : 1;
        messages.push(msg);
        fs.writeFileSync(messagesFile, JSON.stringify(messages, null, 2));
        return msg;
    } catch (e) {
        console.error("Error saving message", e);
        return null;
    }
}

// Load Users
function getUsers() {
    try {
        const data = fs.readFileSync('./accounts.json');
        return JSON.parse(data);
    } catch (e) {
        console.error("Error reading accounts.json", e);
        return [];
    }
}

// Ping Route
app.get('/ping', (req, res) => {
    res.json({ status: 'online', timestamp: Date.now() });
});

// Login Route
app.post('/login', (req, res) => {
    const { nama, password } = req.body;
    const users = getUsers();
    
    const user = users.find(u => (u.name === nama || u.nama === nama) && (bcrypt.compareSync(password, u.password) || u.tag === password));
    
    if (user) {
        const userName = user.name || user.nama;
        const token = jwt.sign({ nama: userName, role: user.role, tag: user.tag }, JWT_SECRET, { expiresIn: '7d' });
        res.json({ success: true, token, user: { nama: userName, role: user.role, tag: user.tag } });
    } else {
        res.status(401).json({ success: false, message: 'Nama, tag, atau password salah.' });
    }
});

let onlineUsers = new Map();

function broadcast(data) {
    const message = JSON.stringify(data);
    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN && client.user) {
            client.send(message);
        }
    });
}

function broadcastOnlineCount() {
    broadcast({ type: 'online_users', count: onlineUsers.size, users: Array.from(onlineUsers.values()) });
}
    

wss.on('connection', (ws, req) => {
    // Authenticate via token in query string or header
    // Since Android OkHttp WebSocket can send headers, but let's check query just in case, or first message.
    let isAuthenticated = false;

    ws.on('message', (messageAsString) => {
        try {
            const data = JSON.parse(messageAsString);
            if (data.type === 'auth') {
                jwt.verify(data.token, JWT_SECRET, (err, decoded) => {
                    if (err) {
                        ws.send(JSON.stringify({ type: 'error', message: 'Authentication error' }));
                        ws.close();
                        return;
                    }
                    ws.user = decoded;
                    isAuthenticated = true;
                    onlineUsers.set(ws.user.nama, { nama: ws.user.nama, role: ws.user.role, tag: ws.user.tag });
                    console.log(`User connected: ${ws.user.nama}`);
                    
                    broadcastOnlineCount();

                    // Send history
                    const rows = getMessages();
                    ws.send(JSON.stringify({ type: 'history', messages: rows }));
                    ws.send(JSON.stringify({ type: 'group_info', ...getGroupInfo() }));
                });
            
            } else if (data.type === 'update_group' && isAuthenticated) {
                const { field, value } = data;
                const role = ws.user.role.toLowerCase();
                
                if (role === 'member') {
                    ws.send(JSON.stringify({ type: 'toast', message: 'Anda bukan admin/owner' }));
                    return;
                }
                if (field === 'banner' && role === 'admin') {
                    ws.send(JSON.stringify({ type: 'toast', message: 'Anda bukan admin/owner' }));
                    return;
                }
                
                // Allowed
                const groupInfo = getGroupInfo();
                
                // Role Validation
                if (field === 'name' || field === 'banner') {
                    if (role !== 'owner') {
                        ws.send(JSON.stringify({ type: 'toast', message: 'Anda bukan admin/owner' }));
                        return;
                    }
                } else if (field === 'description' || field === 'avatar') {
                    if (role !== 'owner' && role !== 'admin') {
                        ws.send(JSON.stringify({ type: 'toast', message: 'Anda bukan admin/owner' }));
                        return;
                    }
                }
                
                if (field === 'description') groupInfo.description = value;
                if (field === 'banner') groupInfo.banner = value;
                if (field === 'avatar') groupInfo.avatar = value;
                if (field === 'name') groupInfo.name = value;
                saveGroupInfo(groupInfo);
                
                ws.send(JSON.stringify({ type: 'toast', message: 'Berhasil diubah' }));
                broadcast({ type: 'group_info', ...groupInfo });
            } else if (data.type === 'send_message' && isAuthenticated) {
                let msg = {
                    nama: ws.user.nama,
                    role: ws.user.role,
                    content: data.content,
                    timestamp: Date.now()
                };
                msg = saveMessage(msg);
                if (msg) {
                    broadcast({ type: 'receive_message', message: msg });
                }
            }
        } catch (e) {
            console.error("Invalid WS message", e);
        }
    });

    ws.on('close', () => {
        if (ws.user) {
            console.log(`User disconnected: ${ws.user.nama}`);
            onlineUsers.delete(ws.user.nama);
            broadcastOnlineCount();
        }
    });
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`Server listening on port ${PORT}`);
});
