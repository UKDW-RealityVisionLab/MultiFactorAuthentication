const express = require('express');
const bodyParser = require('body-parser');
const sqlite3 = require('sqlite3').verbose();
const app = express();
const port = 3000;



// Middleware

const kelasRoute= require('./routes/kelas.js');
const mkRoute= require('./routes/matakuliah.js');
const userRouter= require('./routes/users')
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
app.use('/users',userRouter)
app.use('/dosen',dosenRouter)
app.use('/sesi',sesi)
app.use('/ruang',ruang)
app.use('/jadwal',jadwal)
app.use('/semester',semesterRoute);
app.use('/presensi',presensiRoute)
app.use('/daftarpresensi',daftarPresensi)
app.use('/face',faceVerif)



// Connect to SQLite database
const db = new sqlite3.Database('./mfa_presensi.db', (err) => {
    if (err) {
        console.error('Error opening database:', err.message);
    } else {
        console.log('Connected to the SQLite database.');
    }
});

// Endpoint to update attendance status
app.post('/updateHadirStatus', (req, res) => {
    console.log('Request Body:', req.body);

    const { kode_jadwal, nim_mahasiswa, hadir=1 } = req.body;

    const sql = `UPDATE presensi SET hadir = ? WHERE kode_jadwal = ? AND nim_mahasiswa = ?`;
    const params = [hadir, kode_jadwal, nim_mahasiswa];

    db.run(sql, params, function (err) {
        if (err) {
            console.error('Error updating attendance:', err.message);
            res.status(500).json({ status: 'error', message: 'Failed to update attendance' });
        } else if (this.changes === 0) {
            res.status(404).json({ status: 'error', message: 'Record not found' });
        } else {
            res.status(200).json({ status: 'success', message: 'Attendance updated successfully' });
        }
    });
});

// Start the server
app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});

// Close the database connection on process exit
process.on('SIGINT', () => {
    db.close((err) => {
        if (err) {
            console.error('Error closing database:', err.message);
        } else {
            console.log('Closed the database connection.');
        }
        process.exit(0);
    });
});
