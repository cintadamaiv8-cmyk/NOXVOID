const bcrypt = require('bcryptjs');
try {
    console.log(bcrypt.compareSync('owner123', 'owner123'));
} catch (e) {
    console.log("Error:", e.message);
}
