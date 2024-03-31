
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const db = require('./connect_db');
const app = express();

app.use(bodyParser.json());
app.use(cors());

const PORT = process.env.PORT || 8080;

// GET request
app.get('/data', (req, res) => {
  const sqlQuery = 'SELECT * FROM `presensi` natural join user_mahasiswa; ';

  db.query(sqlQuery, (err, results) => {
    if (err) {
      console.error('Error executing query:', err);
      res.status(500).send('Internal Server Error');
      return;
    }

    res.json(results);
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
