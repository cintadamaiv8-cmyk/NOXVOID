const express = require('express');
const http = require('http');
const { WebSocketServer, WebSocket } = require('ws');
const jwt = require('jsonwebtoken');
const fs = require('fs');
const sqlite3 = require('sqlite3').verbose();
const cors = require('cors');
const bcrypt = require('bcrypt');

const app = express();
app.use(express.json());
app.use(cors());

const server = http.createServer(app);
const wss = new WebSocketServer({ server });

const JWT_SECRET = 'noxvoid_super_secret_key_123';
const PORT = 3000;

// Setup SQLite
const db = new sqlite3.Database('./database.db', (err) => {
    if (err) console.error(err.message);
    else console.log('Connected to SQLite database.');
});

db.serialize(() => {
    db.run(`CREATE TABLE IF NOT EXISTS messages (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nama TEXT,
        role TEXT,
        content TEXT,
        timestamp INTEGER
    )`);
});

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

let onlineUsers = new Set();

function broadcast(data) {
    const message = JSON.stringify(data);
    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN && client.user) {
            client.send(message);
        }
    });
}

function broadcastOnlineCount() {
    broadcast({ type: 'online_users', count: onlineUsers.size });
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
                    onlineUsers.add(ws.user.nama);
                    console.log(`User connected: ${ws.user.nama}`);
                    
                    broadcastOnlineCount();

                    // Send history
                    db.all("SELECT * FROM messages ORDER BY timestamp ASC", (err, rows) => {
                        if (!err) {
                            ws.send(JSON.stringify({ type: 'history', messages: rows }));
                        }
                    });
                });
            } else if (data.type === 'send_message' && isAuthenticated) {
                const msg = {
                    nama: ws.user.nama,
                    role: ws.user.role,
                    content: data.content,
                    timestamp: Date.now()
                };
                db.run("INSERT INTO messages (nama, role, content, timestamp) VALUES (?, ?, ?, ?)", [msg.nama, msg.role, msg.content, msg.timestamp], function(err) {
                    if (!err) {
                        msg.id = this.lastID;
                        broadcast({ type: 'receive_message', message: msg });
                    }
                });
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
