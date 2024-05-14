# Backend for MFA
Applikasi Backend untuk prototipe MFA
## Features
- Manajemen kelas : CRUD data kelas
- Manajemen presensi : CRUD data presensi
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
### Manajemen kelas
- GET localhost:3000/kelas/  
- POST localhost:3000/kelas/  
Request Body
```
    {
        "kode_matakuliah": 556565, //requried numeric
        "group_kelas": "XZ", //required string
        "kode_semester": 7, //required numeric int
        "kode_dosen": 123 //required numeric int
    }
```
- PATCH localhost:3000/kelas/{id}  
Request Body
```
    {
        "group_kelas": "XZ", //required string
        "kode_semester": 7, //required numeric int
        "kode_dosen": 123 //required numeric int
    }
```
- DELETE localhost:3000/kelas/{id}

### Manajemen presensi
- GET localhost:3000/presensi/  
- POST localhost:3000/presensi/ 
Request Body
```
           {
                "id_presensi": 3,
                "jadwal": "jemuah kliwon",
                "nim_mahasiswa": "7121",
                "hadir": 0
            }
```
- PATCH localhost:3000/presensi/{id}  
Request Body
```
{
    "jadwal":"rabu",
    "nim_mahasiswa":"7121",
    "hadir":0
}
```
- DELETE localhost:3000/presensi/{id}
## Tools
- Node.JS  
- Express JS 


## cara testing qr
- endpoint: http://localhost:3000/presensi/kode_jadwal
example:
```
[
{
"data": "android 1 5/13/2024, 10:16:48 PM"
},
{
"data": "android 1 5/13/2024, 10:21:48 PM"
},
{
"user": "Aryo"
}
]
```

## untuk menambahkan mahasiswa di setiap kelasnya:
- insert di db dan pastikan kode_jadwal sama dengan kode jadwal yang ada pada table jadwal



