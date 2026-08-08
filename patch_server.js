const fs = require('fs');
let code = fs.readFileSync('backend/server.js', 'utf8');

code = code.replace(
`                // Allowed
                const groupInfo = getGroupInfo();
                if (field === 'description') groupInfo.description = value;
                if (field === 'banner') groupInfo.banner = value;
                if (field === 'avatar') groupInfo.avatar = value;
                if (field === 'name') {
                    if (role !== 'owner') {
                        ws.send(JSON.stringify({ type: 'toast', message: 'Anda bukan admin/owner' }));
                        return;
                    }
                    groupInfo.name = value;
                }`,
`                // Allowed
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
                if (field === 'name') groupInfo.name = value;`);

fs.writeFileSync('backend/server.js', code);
console.log("Patched server.js for permissions");
