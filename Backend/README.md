# Backend for MFA
Applikasi Backend untuk prototipe MFA
## Tools
- Node.JS  
- Express JS 
## Features
1. Manajemen Kelas
- Melihat semua kelas: Mendapatkan daftar lengkap kelas.
- Menambahkan kelas baru: Membuat entri kelas baru dengan data tertentu.
- Mengedit kelas: Mengubah data kelas tertentu berdasarkan ID.
- Menghapus kelas: Menghapus kelas dari daftar berdasarkan ID.
2. Manajemen Jadwal
- Melihat semua jadwal: Mendapatkan semua data jadwal yang tersedia.
- Menambahkan jadwal baru: Membuat entri jadwal baru dengan informasi lengkap.
- Mengedit jadwal: Memperbarui informasi jadwal berdasarkan kode_jadwal, kecuali kode itu sendiri.
- Menghapus jadwal: Menghapus jadwal berdasarkan kode_jadwal.
3. Manajemen Kelas Sesi
- Melihat data sesi kelas: Mendapatkan informasi tentang sesi kelas.
4. Manajemen Mata Kuliah
- Melihat semua mata kuliah: Mendapatkan daftar lengkap mata kuliah.
- Menambahkan mata kuliah: Membuat entri mata kuliah baru dengan atribut tertentu.
- Mengedit mata kuliah: Mengubah data mata kuliah yang ada berdasarkan kode_matakuliah.
- Menghapus mata kuliah: Menghapus data mata kuliah berdasarkan kode_matakuliah.
5. Manajemen Ruang
- Melihat semua data ruang: Mendapatkan daftar ruang.
- Melihat data ruang tertentu: Mendapatkan informasi spesifik berdasarkan kodeRuang.
- Menambahkan data ruang: Menambah entri ruang baru.
- Mengedit data ruang: Memperbarui data ruang kecuali kodeRuang.
- Menghapus data ruang: Menghapus ruang berdasarkan kode_ruang.
- Mendapatkan lokasi berdasarkan pertemuan: Menampilkan latitude dan longitude berdasarkan ID pertemuan.
6. Manajemen Semester
- Melihat semua data semester: Mendapatkan daftar semester.
- Menambahkan semester baru: Membuat entri semester baru.
- Mengedit data semester: Mengubah informasi semester kecuali kode_semester.
- Melihat data semester tertentu: Mendapatkan informasi spesifik berdasarkan kode_semester.
- Menghapus data semester: Menghapus semester berdasarkan kode_semester.
7. Manajemen Dosen
- Melihat semua data dosen: Mendapatkan daftar dosen.
- Menambahkan data dosen: Membuat entri dosen baru.
- Mengedit data dosen: Memperbarui informasi dosen kecuali nidn.
- Menghapus data dosen: Menghapus data dosen berdasarkan nidn.
8. Manajemen Mahasiswa
- Melihat semua data mahasiswa: Mendapatkan daftar mahasiswa.
- Menambahkan data mahasiswa: Membuat entri mahasiswa baru.
- Mengedit data mahasiswa: Memperbarui informasi mahasiswa kecuali nim.
- Menghapus data mahasiswa: Menghapus data mahasiswa berdasarkan nim.
- Melihat data mahasiswa tertentu: Mendapatkan informasi spesifik berdasarkan nim.
9. Manajemen Daftar Presensi
- Melihat daftar presensi berdasarkan jadwal: Mendapatkan presensi berdasarkan kode_jadwal.
- Melihat profil pengguna: Mendapatkan profil pengguna berdasarkan email.
- Mengecek status presensi: Menampilkan status kehadiran mahasiswa berdasarkan idJadwal dan nim.
- Mengedit status presensi: Memperbarui status presensi tertentu berdasarkan ID.
- Menghapus presensi: Menghapus data presensi berdasarkan ID.
- Menambahkan daftar presensi baru: Membuat entri presensi baru.
10. QR Code
- Melihat QR Code presensi: Mendapatkan data QR Code berdasarkan kode_jadwal.
- Menvalidasi request body untuk qr code
11. Face Verify
- Verifikasi wajah mahasiswa: Mengecek verifikasi wajah berdasarkan idJadwal dan nim lalu menupdate kehadiran.

*Setiap fitur mencakup fungsi CRUD (Create, Read, Update, Delete) dan fitur tambahan untuk memenuhi kebutuhan manajemen data secara menyeluruh di sistem.*
## Articles

## How to start
Jalankan perintah melalui console pada directory project  
```
npm install
```

lalu jalankan perintah 
```
npm run serve
```
Akses API yang ada pada daftar endpoint melalui aplikasi Postman  

## API Endpoint List
### 1. Manajemen kelas
- GET localhost:3000/kelas/  
    Mendapatkan semua kelas yang ada
- POST localhost:3000/kelas/  
   Menambah kelas
**Request Body:**
```
    {
        "kode_matakuliah": 556565, //requried numeric
        "group_kelas": "XZ", //required string
        "kode_semester": 7, //required numeric int
        "kode_dosen": 123 //required numeric int
    }
```
- PATCH localhost:3000/kelas/{id}  
    edit kelas
**Request Body**
```
    {
        "group_kelas": "XZ", //required string
        "kode_semester": 7, //required numeric int
        "kode_dosen": 123 //required numeric int
    }
```
- DELETE localhost:3000/kelas/{id}
    Menghapus kelas

### 2. Manajemen Jadwal
- GET http://localhost:3000/jadwal
    Mendapatkan semua data jadwal
- POST http://localhost:3000/jadwal
    Menambah jadwal
**Request Body for post**
```
   {
  "kode_jadwal": "J123",
  "kode_kelas": "K456",
  "kode_ruang": "R789",
  "kode_sesi": "S101",
  "tanggal": "2024-11-08"
}

```
- PATCH http://localhost:3000/jadwalKuliah/{kode_jadwal}
edit jadwal berdasarkan params kode_jadwal(dilarang mengubah kode_jadwal)
**Req body**
```
{
"kode_ruang": "kode_ruang_baru",
"kode_sesi": "kode_sesi_baru",
"tanggal": "2024-04-24 08:00:00",
"kode_kelas": "5"
}
```
- DELETE http://localhost:3000/jadwal/{kode_jadwal}

### 3. Manajemen kelas_sesi
- GET http://localhost:3000/sesi
    mendapatkan data kelas sesi

### 4. Manajemen Mata Kuliah
- GET http://localhost:3000/mataKuliah
mendapatkan semua data matkul
- POST http://localhost:3000/mataKuliah
**Request Body for Mata Kuliah**
```
{
"kode_matakuliah": 6969,
"nama_matakuliah": "testing",
"sks": 3,
"harga": 8888888,
"is_praktikum": 0,
"minimal_sks": 50,
"tanggal_input": "2024-05-04"
}
```
- PATCH http://localhost:3000/mataKuliah/{kode_matakuliah}
**Request Body for patch kode matakuliah**
```
{
"kode_matakuliah": 2121,
"nama_matakuliah": "android",
"sks": 3,
"harga": 150000,
"is_praktikum": 0,
"minimal_sks": 50,
"tanggal_input": "2024-05-04"
}
```
- DELETE http://localhost:3000/mataKuliah/{kode_matakuliah}

### 5. Manajemen RUANG
- GET http://localhost:3000/ruang
mendapatkan semua data ruang
- GET http://localhost:3000/ruang/getEdit/{kodeRuang}
get by id berdasarkan params
- POST http://localhost:3000/ruang
**Request Body**
```
{
"kodeRuang": "D.3.1",
"nama": "Didaktos 1",
"latitude": "",
"longitude": ""
}
```
menambah data ruang
- PATCH http://localhost:3000/ruang/{kode_ruang}
Request Body
```
{
"nama": "Didaktos 1",
"latitude": 1,
"longitude": 1
}
```
dilarang mengubah kode ruang
- DELETE http://localhost:3000/ruang/{kode_ruang}

- get latitute longtitude berdasarkan pertemuan
POST http://localhost:3000/ruang/selectRuang
**req body**
```
{
    "idJadwal":"android 1"
}
```
id jadwal merupakan id pertemuan

### 6. Manajemen Semester
- GET http://localhost:3000/semester
mendapatkan semua data semester
- POST http://localhost:3000/semester
**Request Body**
```
{
"kode_semester": 5,
"tahun_ajaran": "2024/2025",
"tanggal_mulai": "2024-08-01",
"tanggal_selesai": "2024-12-20"
}
```
- PATCH http://localhost:3000/semester/{kode_semester}
```
{
"tahun_ajaran": "2024/2025",
"tanggal_mulai": "2024-08-01",
"tanggal_selesai": "2024-12-20"
}
```
edit data kecuali kode_semester
- get by kode semester
GET http://localhost:3000/semester/{kode_semester}
- DELETE http://localhost:3000/semester/{kode_semester}

### 7. Manajemen Dosen
- GET http://localhost:3000/dosen
- POST http://localhost:3000/dosen
**Request Body**
```
{
"nidn": "80600707",
"kode_prodi": "7",
"nama": "Hendro"
}
```
- DELETE http://localhost:3000/dosen/{nidn}
- PATCH http://localhost:3000/dosen/{nidn}
```
{
"kode_prodi": "7",
"nama": "Hendro"
}
```
NIDN tidak dapat di edit
### 8. Manajemen Mahasiswa
- GET http://localhost:3000/mahasiswa
- POST http://localhost:3000/mahasiswa
Request Body
```
{
"nim": "71210713",
"kode_prodi": "71",
"tahun_angkatan": 2021,
"nama": "Aryo"
}
```
- PATCH http://localhost:3000/mahasiswa/{nim}
**Request Body**
```
{
"kode_prodi": "71",
"tahun_angkatan": 2021,
"nama": "Aryo"
}
```
- DELETE http://localhost:3000/mahasiswa/{nim}
- get by nim 
GET http://localhost:3000/mahasiswa/{nim}

### 9. Manajemen daftar presensi
- GET localhost:3000/daftarpresensi/{kode_jadwal}
get daftar presensi berdasarkan jadwal melalui params
- get profil user
POST http://localhost:3000/daftarpresensi/getProfile/email
```
{
    "email": "example@gmail.com"
}
```
- cek status presensi
POST http://localhost:3000/daftarpresensi/cekStatusPresensi
**Req body**
```
{
    "idJadwal":"PROGWEB 1",
    "nim":"71210713"
}
```
- PATCH localhost:3000/daftarpresensi/{id}  
Request Body
```
{
    "hadir":"hadir"
}
```
- DELETE localhost:3000/daftarpresensi/{id}
- add presensi
POST localhost:3000/daftarpresensi/
**REQ BODY**
```
{
    "jadwal":"PROGWEB 1",
    "nim":"71210713",
    "hadir":0
}
```

## 10. QR code
- GET localhost:3000/presensi/kode_jadwal
- POST localhost:3000/presensi/kode_jadwal
**Req body**
```
"qrCodeData": "PROGWEB-1-5/25/2024, 1:34:10 AM,
"kodeJadwal": "PROGWEB:1"
```

## 11. face verify
- POST localhost:3000/face/faceVerify
**req body**
```
{
    "idJadwal":"android 1",
    "nim":"71210713"
}
```

## untuk menambahkan mahasiswa di setiap kelasnya:
- insert di db dan pastikan kode_jadwal sama dengan kode jadwal yang ada pada table jadwal
