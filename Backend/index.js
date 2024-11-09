const express= require('express');
const app = express();

const port= 3000;
// const presensiRoute= require('./routes/presensi.js');

const kelasRoute= require('./routes/kelas.js');
const mkRoute= require('./routes/matakuliah.js');
const mahasiswaRouter= require('./routes/mahasiswa.js');
const dosenRouter= require('./routes/dosen.js')
const logReq= require('./middleware/log.js')
const sesi= require('./routes/sesi')
const ruang= require('./routes/ruang')
const jadwal = require('./routes/jadwal.js')
const semesterRoute = require('./routes/semester.js')
const presensiRoute = require('./routes/qr.js')
const daftarPresensi= require('./routes/presensi.js')
const faceVerif= require('./routes/faceVerify.js')

// cuma log aja
app.use(logReq);
// ini untuk permission json body
app.use(express.json())
// app.use('/presensi',presensiRoute);
app.use('/matakuliah',mkRoute);
app.use('/kelas',kelasRoute);
app.use('/mahasiswa', mahasiswaRouter)
app.use('/dosen',dosenRouter)
app.use('/sesi',sesi)
app.use('/ruang',ruang)
app.use('/jadwal',jadwal)
app.use('/semester',semesterRoute);
app.use('/presensi',presensiRoute)
app.use('/daftarpresensi',daftarPresensi)
app.use('/face',faceVerif)



app.listen(port,()=>{
    console.log('server sudah berjalan di port: '+ port)
})