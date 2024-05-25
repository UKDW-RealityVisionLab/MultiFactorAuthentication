// API_manajemen_presensi.js

const express = require('express');
const cors = require('cors');
const db = require("../config/db");
const app = express();

app.use(cors());


const dataPresensi =  async (req, res) => {
  try {
    const {kode_jadwal} = req.params
    const queryGet = `SELECT presensi.email, presensi.id_presensi,presensi.nim_mahasiswa,presensi.nama, presensi.hadir FROM jadwal INNER JOIN presensi on jadwal.kode_jadwal=presensi.kode_jadwal WHERE jadwal.kode_jadwal='${kode_jadwal}';`;
    const result = await new Promise((resolve, reject) => {
  
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    res.json(result)
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
}
};

const getProfile = async (req, res) => {
  try {
      let email;
      let source;

      if (req.body.email) {
          email = req.body.email;
          source = "req.body";
      } else if (req.params.email) {
          email = req.params.email;
          source = "req.params";
      }

      if (!email) {
          return res.status(400).json({ message: "email is required" });
      }

      console.log("Received email:", email, "from", source);

      const queryGet = `SELECT * FROM presensi WHERE email = ?;`;
      
      const result = await new Promise((resolve, reject) => {
          db.all(queryGet, [email], (error, rows) => {
              if (error) {
                  reject(error);
              } else {
                  resolve(rows);
              }
          });
      });

      if (result.length === 0) {
          return res.status(404).json({ message: "Data not found" });
      }

      let respond = {};
      result.forEach(result => {
        respond= {
          email: result.email,
          nama: result.nama,
          nim: result.nim_mahasiswa
        };
      });

      res.json(respond);
  } catch (error) {
      console.error(error);
      return res.status(500).json({ message: "Terjadi kesalahan" });
  }
}

const cekStatusPresensi = async (req, res) => {
  const { idJadwal, nim } = req.body;
  let query = `SELECT presensi.hadir, presensi.kode_jadwal FROM presensi WHERE presensi.kode_jadwal = ? AND nim_mahasiswa = ?`;
  try {
    const result = await new Promise((resolve, reject) => {
      db.all(query, idJadwal, nim, (error, rows) => {
        if (error) {
          reject(error);
        } else {
          resolve(rows);
        }
      });
    });
    if (result.length === 0) {
      res.json({ message: "data tidak ditemukan" });
    } else {
      const hadir = result[0].hadir;
      if (hadir === "0") {
        res.json(false);
      } else if (hadir === "hadir") {
        res.json(true);
      }
    }
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Error occurred" });
  }
};

const insertPresensi =  async (req, res) => {
  try {
    const {  jadwal, nim, hadir } = req.body; // Retrieve data from request body
    console.log('Received data:', {  jadwal, nim, hadir });

    const nim_mahasiswa = nim; 
    console.log('Student ID:', nim_mahasiswa);

    // Perform validation if needed
    
    const queryInsert = "INSERT INTO presensi ( jadwal, nim_mahasiswa, hadir) VALUES (?, ?, ?)";
    console.log('Insertion query:', queryInsert);

    await new Promise((resolve, reject) => {
      db.run(queryInsert, [ jadwal, nim_mahasiswa, hadir], function (error) {
        if (error) reject(error);
        resolve({ id: this.lastID }); // Return the ID of the inserted row
      });
    });
    
    res.json({
      message:"success add data presensi"
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};


const deletePresensi = async (req, res) => {
  try {
    const { id } = req.params; // Extract the ID from the request parameters

    const queryDelete = "DELETE FROM presensi WHERE id_presensi = ?";
    const result = await new Promise((resolve, reject) => {
      db.run(queryDelete, id, function (error) {
        if (error) reject(error);
        resolve({ affectedRows: this.changes }); // Return the number of affected rows
      });
    });

    if (result.affectedRows > 0) {
      res.json({message:`success delete ${id}`});
    } else {
      return res.status(404).json({ message: "Data tidak ditemukan" });
    }
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const updatePresensi =  async (req, res) => {
  try {
    const{id}=req.params;

    const { jadwal, nim_mahasiswa, hadir } = req.body; // Extract the ID from the request parameters
    const queryUpdate = `UPDATE presensi SET jadwal = ?, nim_mahasiswa = ?, hadir = ? WHERE id_presensi = '${id}'`;
    const result = await new Promise((resolve, reject) => {
      db.run(queryUpdate, [jadwal, nim_mahasiswa, hadir], function (error) {
        if (error) reject(error);
        resolve({ affectedRows: this.changes }); // Return the number of affected rows
      });
    });

    if (result.affectedRows > 0) {
      res.json({message:`success edit ${id}`});
    } else {
      return res.status(404).json({ message: "Data tidak ditemukan" });
    }
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

module.exports = { dataPresensi, insertPresensi, deletePresensi, updatePresensi,getProfile,cekStatusPresensi };
