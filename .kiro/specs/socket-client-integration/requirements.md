# Requirements Document

## Introduction

Frontend FinTrack saat ini menggunakan store pattern lokal (TransactionStore, AccountStore, CategoryStore) untuk manajemen state, namun belum terhubung dengan backend socket server. Fitur ini akan mengintegrasikan frontend dengan backend melalui koneksi socket TCP ke port 3000, sehingga semua data transaksi, akun, dan kategori disimpan dan dikelola oleh backend server.

Integrasi ini akan:
- Mengganti store pattern lokal dengan komunikasi socket ke backend
- Mengimplementasikan session management untuk autentikasi user
- Menyinkronkan semua operasi CRUD dengan backend
- Menangani error dan koneksi yang terputus dengan graceful

## Glossary

- **SocketClient**: Komponen yang mengelola koneksi socket TCP ke backend server
- **SessionManager**: Komponen frontend yang menyimpan session token setelah login
- **Backend Protocol**: Format komunikasi pipe-delimited (`COMMAND|param1|param2|...`)
- **Store Pattern**: Pattern saat ini di frontend untuk state management lokal
- **Socket Connection**: Koneksi TCP persistent ke backend server di port 3000
- **Session Token**: UUID yang diterima setelah login untuk autentikasi request
- **Command Response**: Response dari backend dalam format `STATUS|data1|data2|...`
- **Connection Pool**: Mekanisme untuk mengelola koneksi socket yang dapat di-reuse
- **Reconnection Strategy**: Strategi untuk reconnect otomatis saat koneksi terputus

## Requirements

### Requirement 1

**User Story:** Sebagai pengguna aplikasi, saya ingin login ke sistem, sehingga saya dapat mengakses data keuangan saya yang tersimpan di server.

#### Acceptance Criteria

1. WHEN user memasukkan username dan password yang valid dan menekan tombol login THEN Frontend SHALL mengirim command LOGIN ke backend dan menerima session token
2. WHEN login berhasil THEN Frontend SHALL menyimpan session token di SessionManager untuk digunakan pada request berikutnya
3. WHEN login gagal THEN Frontend SHALL menampilkan pesan error yang jelas kepada user
4. WHEN user belum terdaftar THEN Frontend SHALL menyediakan tombol register yang mengirim command REGISTER ke backend
5. WHEN user logout THEN Frontend SHALL mengirim command LOGOUT dengan session token dan menghapus session token dari SessionManager

### Requirement 2

**User Story:** Sebagai developer, saya ingin membuat komponen SocketClient yang reusable, sehingga semua komunikasi dengan backend terpusat dan mudah di-maintain.

#### Acceptance Criteria

1. WHEN SocketClient diinisialisasi THEN SocketClient SHALL membuat koneksi TCP ke localhost port 3000
2. WHEN SocketClient mengirim command THEN SocketClient SHALL memformat command dalam format pipe-delimited dan mengirimnya melalui socket
3. WHEN SocketClient menerima response THEN SocketClient SHALL mem-parse response dan mengembalikannya dalam format yang mudah digunakan
4. WHEN koneksi socket terputus THEN SocketClient SHALL mencoba reconnect secara otomatis dengan exponential backoff
5. WHEN SocketClient ditutup THEN SocketClient SHALL menutup koneksi socket dengan graceful
6. WHEN multiple thread menggunakan SocketClient THEN SocketClient SHALL menggunakan synchronized method untuk thread safety

### Requirement 3

**User Story:** Sebagai pengguna, saya ingin mengelola transaksi melalui UI, sehingga semua perubahan tersimpan di backend server.

#### Acceptance Criteria

1. WHEN user menambah transaksi baru THEN Frontend SHALL mengirim command ADD dengan session token dan data transaksi ke backend
2. WHEN user mengupdate transaksi THEN Frontend SHALL mengirim command UPDATE dengan session token dan data transaksi yang diupdate
3. WHEN user menghapus transaksi THEN Frontend SHALL mengirim command DELETE dengan session token dan transaction ID
4. WHEN TransaksiPage dibuka THEN Frontend SHALL mengirim command GET_ALL dengan session token untuk memuat semua transaksi dari backend
5. WHEN backend mengembalikan data transaksi THEN Frontend SHALL mem-parse response dan menampilkannya di tabel
6. WHEN operasi transaksi gagal THEN Frontend SHALL menampilkan pesan error yang informatif kepada user

### Requirement 4

**User Story:** Sebagai pengguna, saya ingin mengelola akun/wallet melalui UI, sehingga saya dapat mengorganisir keuangan saya berdasarkan akun.

#### Acceptance Criteria

1. WHEN user menambah akun baru THEN Frontend SHALL mengirim command ADD_ACCOUNT dengan session token dan data akun ke backend
2. WHEN user mengupdate akun THEN Frontend SHALL mengirim command UPDATE_ACCOUNT dengan session token dan data akun yang diupdate
3. WHEN user menghapus akun THEN Frontend SHALL mengirim command DELETE_ACCOUNT dengan session token dan account ID
4. WHEN AkunWalletPage dibuka THEN Frontend SHALL mengirim command GET_ACCOUNTS dengan session token untuk memuat semua akun dari backend
5. WHEN backend mengembalikan data akun THEN Frontend SHALL mem-parse response dan menampilkannya di UI

### Requirement 5

**User Story:** Sebagai pengguna, saya ingin mengelola kategori transaksi melalui UI, sehingga saya dapat mengkategorikan transaksi sesuai kebutuhan saya.

#### Acceptance Criteria

1. WHEN user menambah kategori baru THEN Frontend SHALL mengirim command ADD_CATEGORY dengan session token, type, dan name ke backend
2. WHEN user menghapus kategori THEN Frontend SHALL mengirim command DELETE_CATEGORY dengan session token, type, dan name
3. WHEN KategoriPage dibuka THEN Frontend SHALL mengirim command GET_CATEGORIES dengan session token untuk memuat semua kategori dari backend
4. WHEN backend mengembalikan data kategori THEN Frontend SHALL mem-parse response dan memisahkan kategori income dan expense

### Requirement 6

**User Story:** Sebagai pengguna, saya ingin melihat summary keuangan di dashboard, sehingga saya dapat memantau kondisi keuangan saya.

#### Acceptance Criteria

1. WHEN HomePage dibuka THEN Frontend SHALL mengirim command GET_MONTHLY_SUMMARY dengan session token dan yearMonth untuk 6 bulan terakhir
2. WHEN backend mengembalikan monthly summary THEN Frontend SHALL mem-parse response dan menampilkan chart trend 6 bulan
3. WHEN HomePage membutuhkan category breakdown THEN Frontend SHALL mengirim command GET_CATEGORY_BREAKDOWN dengan session token dan yearMonth
4. WHEN backend mengembalikan category breakdown THEN Frontend SHALL mem-parse response dan menampilkan pie chart pengeluaran per kategori

### Requirement 7

**User Story:** Sebagai pengguna, saya ingin aplikasi menangani error dengan baik, sehingga saya mendapat feedback yang jelas saat terjadi masalah.

#### Acceptance Criteria

1. WHEN backend mengembalikan ERROR response THEN Frontend SHALL mem-parse error code dan description lalu menampilkan dialog error yang informatif
2. WHEN session token expired atau invalid THEN Frontend SHALL menampilkan pesan "Session expired" dan redirect ke halaman login
3. WHEN koneksi ke backend gagal THEN Frontend SHALL menampilkan pesan "Cannot connect to server" dan menyediakan tombol retry
4. WHEN operasi timeout THEN Frontend SHALL menampilkan pesan "Request timeout" dan membatalkan operasi
5. WHEN backend tidak merespon THEN Frontend SHALL menunggu maksimal 10 detik sebelum timeout

### Requirement 8

**User Story:** Sebagai developer, saya ingin mengganti implementasi Store pattern, sehingga data tidak lagi disimpan lokal tapi diambil dari backend.

#### Acceptance Criteria

1. WHEN TransactionStore method dipanggil THEN TransactionStore SHALL memanggil SocketClient untuk berkomunikasi dengan backend
2. WHEN AccountStore method dipanggil THEN AccountStore SHALL memanggil SocketClient untuk berkomunikasi dengan backend
3. WHEN CategoryStore method dipanggil THEN CategoryStore SHALL memanggil SocketClient untuk berkomunikasi dengan backend
4. WHEN Store menerima response dari backend THEN Store SHALL mem-parse data dan notify semua listeners dengan snapshot terbaru
5. WHEN Store method gagal THEN Store SHALL throw exception dengan pesan error yang jelas

### Requirement 9

**User Story:** Sebagai pengguna, saya ingin aplikasi tetap responsif saat berkomunikasi dengan server, sehingga UI tidak freeze.

#### Acceptance Criteria

1. WHEN Frontend mengirim request ke backend THEN Frontend SHALL menggunakan background thread untuk operasi socket
2. WHEN operasi socket selesai THEN Frontend SHALL mengupdate UI di Event Dispatch Thread (EDT)
3. WHEN operasi sedang berjalan THEN Frontend SHALL menampilkan loading indicator di UI
4. WHEN operasi selesai THEN Frontend SHALL menyembunyikan loading indicator
5. WHEN user melakukan operasi lain saat request sedang berjalan THEN Frontend SHALL mengantri request atau membatalkan request sebelumnya

### Requirement 10

**User Story:** Sebagai developer, saya ingin membuat AuthPage untuk login dan register, sehingga user dapat autentikasi sebelum menggunakan aplikasi.

#### Acceptance Criteria

1. WHEN AuthPage ditampilkan THEN AuthPage SHALL menampilkan form login dengan field username dan password
2. WHEN user menekan tombol "Belum punya akun" THEN AuthPage SHALL menampilkan form register
3. WHEN user submit form register THEN AuthPage SHALL mengirim command REGISTER ke backend dengan username dan password
4. WHEN register berhasil THEN AuthPage SHALL otomatis login dan redirect ke aplikasi utama
5. WHEN AuthPage menerima error dari backend THEN AuthPage SHALL menampilkan pesan error di bawah form

### Requirement 11

**User Story:** Sebagai pengguna, saya ingin aplikasi menyimpan session saya, sehingga saya tidak perlu login ulang setiap kali membuka aplikasi.

#### Acceptance Criteria

1. WHEN user berhasil login THEN Frontend SHALL menyimpan session token ke file lokal
2. WHEN aplikasi dibuka THEN Frontend SHALL membaca session token dari file lokal
3. WHEN session token ditemukan THEN Frontend SHALL mengirim command VALIDATE_SESSION ke backend untuk memvalidasi token
4. WHEN session token valid THEN Frontend SHALL langsung menampilkan aplikasi utama tanpa login
5. WHEN session token invalid atau expired THEN Frontend SHALL menampilkan halaman login
6. WHEN user logout THEN Frontend SHALL menghapus session token dari file lokal

### Requirement 12

**User Story:** Sebagai developer, saya ingin menggunakan format data yang konsisten dengan backend, sehingga tidak ada mismatch data.

#### Acceptance Criteria

1. WHEN Frontend mengirim amount THEN Frontend SHALL mengirim sebagai long integer (bukan double)
2. WHEN Frontend menerima amount dari backend THEN Frontend SHALL mem-parse sebagai long integer
3. WHEN Frontend mengirim date THEN Frontend SHALL menggunakan format yyyy-MM-dd
4. WHEN Frontend mengirim transaksi THEN Frontend SHALL menyertakan accountName dan accountType
5. WHEN Frontend mem-parse response DATA_ALL THEN Frontend SHALL mem-parse semua field sesuai urutan yang didefinisikan backend
