# NoxVoid Backend (Termux)

## Persyaratan
- Node.js
- SQLite3

## Cara Install di Termux
1. Buka Termux.
2. Jalankan `pkg update && pkg upgrade`
3. Install NodeJS: `pkg install nodejs`
4. Pindahkan/salin folder `backend` ini ke dalam storage lokal Termux Anda.
5. Masuk ke folder backend: `cd backend`
6. Install dependensi: `npm install`

## Cara Menjalankan
1. Jalankan server: `npm start`
2. Server akan berjalan di port `3000`.

## Manajemen Akun
Untuk menambah atau mengedit akun (Owner, Admin, Member), Anda **cukup mengedit file `accounts.json`**.
Tidak perlu merubah source code server. Server akan langsung membaca konfigurasi terbaru setiap kali user mencoba login.

Contoh data akun:
```json
{
  "nama": "Vanxz",
  "tag": "#39AS72S",
  "password": "owner123",
  "role": "owner"
}
```

## Koneksi APK
Pastikan Anda mengubah `BASE_URL` dan `WS_URL` di dalam file `app/src/main/java/com/example/data/AppConfig.kt` sesuai dengan IP Address Termux Anda sebelum melakukan Build APK.
