const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');

const app = express();

app.use(bodyParser.json());
app.use(cors());

const PORT = process.env.PORT || 8080;



//Urusan DB
const sqlite3 = require('sqlite3').verbose();

// Open a database connection
const db = new sqlite3.Database('./db/Mahasiswa.db', (err) => {
  if (err) {
      console.error('Error connecting to database:', err.message);
  } else {
      console.log('Connected to the SQLite database');
  }
});

//buat tabel



// GET request
app.get('/daftarPresensi', (req, res) => {
  db.all("SELECT * FROM presensiMahasiswa", (err, rows) => {
      if (err) {
          res.status(500).json({ error: err.message });
          return;
      }
      res.json(rows);
  });
});

// POST request
app.post('/', (req, res) => {
  res.send('POST request to the homepage');
});

// PUT request
app.put('/', (req, res) => {
  res.send('PUT request to the homepage');
});

// PATCH request
app.patch('/', (req, res) => {
  res.send('PATCH request to the homepage');
});

// DELETE request
app.delete('/', (req, res) => {
  res.send('DELETE request to the homepage');
});

// HEAD request
app.head('/', (req, res) => {
  res.send('HEAD request to the homepage');
});

// OPTIONS request
app.options('/', (req, res) => {
  res.send('OPTIONS request to the homepage');
});


app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});