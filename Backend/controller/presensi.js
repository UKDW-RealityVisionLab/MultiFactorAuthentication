const express = require('express');
const cors = require('cors');
const db = require("../config/db");
const app = express();

app.use(cors());


const dataPresensi =  async (req, res) => {
  try {
    const {kode_jadwal} = req.params
    const queryGet = `SELECT presensi.id_presensi, presensi.kode_jadwal, presensi.nim_mahasiswa, presensi.hadir, user_mahasiswa.nama, user_mahasiswa.email
    FROM presensi
    INNER JOIN user_mahasiswa ON presensi.nim_mahasiswa = user_mahasiswa.nim WHERE presensi.kode_jadwal='${kode_jadwal}';`;
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
    } else {
        console.log("Received email:", email, "from", source);

        const queryGet = `SELECT user_mahasiswa.email, user_mahasiswa.nim, user_mahasiswa.nama
        FROM user_mahasiswa
        WHERE user_mahasiswa.email = ?;`
        
        const result = await new Promise((resolve, reject) => {
            db.all(queryGet, [email], (error, rows) => {
                if (error) {
                    reject(error);
                } else {
                    resolve(rows);
                }
            });
        });

        // Check if user hasn't registered
        if (result.length === 0) {
            let nameOfEmail = email.substring(0, email.lastIndexOf("@"));
            // let defaultNim = 71210714;
            const regisToDb = `INSERT INTO user_mahasiswa (nim, kode_prodi, tahun_angkatan, nama, email) VALUES (?, 71, 2021, ?, ?)`;

            const cekLastNim= `SELECT user_mahasiswa.nim FROM user_mahasiswa ORDER BY nim DESC LIMIT 1;`
            const getLastNim= await new Promise((resolve, reject)=>{
              db.all(cekLastNim,(error,row)=>{
                if (error) {
                  reject(error);
              } else {
                  resolve(row);
              }
              })
            })
            
            let lastNim = getLastNim.length > 0 ? parseInt(getLastNim[0].nim, 10) : 0;

            // Increment the NIM to get the new NIM
            let newNim = lastNim + 1;
              
            // run regis
            await new Promise((resolve, reject) => {
                db.run(regisToDb,[newNim, nameOfEmail, email], (error) => {
                    if (error) {
                        return reject(error);
                    } else {
                        return resolve();
                    }
                });
            });

            // Fetch the newly registered user
            const newResult = await new Promise((resolve, reject) => {
                db.all(queryGet, [email], (error, rows) => {
                    if (error) {
                        reject(error);
                    } else {
                        resolve(rows);
                    }
                });
            });

            result.push(...newResult);
        }

        let respond = {};
        result.forEach(row => {
            respond = {
                nama: row.nama,
                nim: row.nim,
                email: row.email
            };
        });

        res.json(respond);
    }
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
