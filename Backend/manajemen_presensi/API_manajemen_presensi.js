// API_manajemen_presensi.js

const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const db = require('./connect_db'); // Import the db object directly
const app = express();

app.use(bodyParser.json());
app.use(cors());

const PORT = process.env.PORT || 8080;

// GET request
// app.get('/data', (req, res) => {
  // const sqlQuery = 'SELECT * FROM presensi; ';

  // db.all(sqlQuery, (err, rows) => { // Use db.all instead of db.query
  //   if (err) {
  //     console.error('Error executing query:', err);
  //     res.status(500).send('Internal Server Error');
  //     return;
  //   }

  //   res.json(rows);
  // });
// });



const formatRes = (status, data, message, res) => {
  res.status(status).json({
    kelas: {
      status: status,
      dataKelas: data,
      message: message,
    },
  });
};

app.get('/data_presensi', async (req, res) => {
  try {
    const queryGet = "SELECT * FROM presensi";
    const result = await new Promise((resolve, reject) => {
  
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    formatRes(200, result, "berhasil get presensi", res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
}
});



app.post('/data_presensi', async (req, res) => {
  try {
    const {  jadwal, nim, hadir } = req.body; // Retrieve data from request body
    console.log('Received data:', {  jadwal, nim, hadir });

    const nim_mahasiswa = nim; 
    console.log('Student ID:', nim_mahasiswa);

    // Perform validation if needed
    
    const queryInsert = "INSERT INTO presensi ( jadwal, nim_mahasiswa, hadir) VALUES (?, ?, ?)";
    console.log('Insertion query:', queryInsert);

    const result = await new Promise((resolve, reject) => {
      db.run(queryInsert, [ jadwal, nim_mahasiswa, hadir], function (error) {
        if (error) reject(error);
        resolve({ id: this.lastID }); // Return the ID of the inserted row
      });
    });
    
    formatRes(200, result, "Data berhasil ditambahkan", res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
});






// Other routes...


app.delete('/data_presensi', async (req, res) => {
  try {
    const { id } = req.body; // Extract the ID from the request parameters

    const queryDelete = "DELETE FROM presensi WHERE id_presensi = ?";
    const result = await new Promise((resolve, reject) => {
      db.run(queryDelete, id, function (error) {
        if (error) reject(error);
        resolve({ affectedRows: this.changes }); // Return the number of affected rows
      });
    });

    if (result.affectedRows > 0) {
      formatRes(200, result, "Data berhasil dihapus", res);
    } else {
      return res.status(404).json({ message: "Data tidak ditemukan" });
    }
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
});






app.put('/data_presensi', async (req, res) => {
  try {
    const { jadwal, nim_mahasiswa, hadir,id_presensi } = req.body; // Extract the ID from the request parameters

    const queryUpdate = "UPDATE presensi SET jadwal = ?, nim_mahasiswa = ?, hadir = ? WHERE id_presensi = ?";
    const result = await new Promise((resolve, reject) => {
      db.run(queryUpdate, [jadwal, nim_mahasiswa, hadir, id_presensi], function (error) {
        if (error) reject(error);
        resolve({ affectedRows: this.changes }); // Return the number of affected rows
      });
    });

    if (result.affectedRows > 0) {
      formatRes(200, result, "Data berhasil diupdate", res);
    } else {
      return res.status(404).json({ message: "Data tidak ditemukan" });
    }
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
});

app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});