# Backend for MFA
Applikasi Backend untuk prototipe MFA
## Features
- Manajemen kelas : CRUD data kelas

## Articles

## How to start
Jalankan perintah melalui console pada directory project  
```
npm install
```

lalu jalankan perintah 
```
npm run start
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
## Tools
- Node.JS  
- Express JS 

