const db = require("../config/db");
const presensIfvalid = async (req, res) => {
    try {
      const idJadwal = req.body.idJadwal;
      const nim = req.body.nim;
  
      if (idJadwal && nim != null) {
        const getPresensiData = `SELECT * FROM presensi WHERE kode_jadwal = '${idJadwal}' AND nim_mahasiswa = '${nim}'`;
        const result = await new Promise((resolve, reject) => {
          db.all(getPresensiData, (error, rows) => {
            if (error) reject(error);
            resolve(rows);
          });
        });
  
        if (result.length > 0) {
          const queryUpdate = `UPDATE presensi SET hadir = 'hadir' WHERE nim_mahasiswa = '${nim}' AND kode_jadwal = '${idJadwal}'`;
          const updateResult = await new Promise((resolve, reject) => {
            db.run(queryUpdate, function (error) {
              if (error) reject(error);
              resolve({ affectedRows: this.changes });
            });
          });
  
          if (updateResult.affectedRows > 0) {
            res.json(true);
          } else {
            res.status(500).json({ message: `Gagal update presensi` });
          }
        } else {
          res.status(404).json({ message: `Data tidak ditemukan` });
        }
      } else {
        res.status(400).json({ message: `NIM atau ID Jadwal tidak boleh kosong` });
      }
    } catch (error) {
      console.error(error);
      res.status(500).json({ message: `Terjadi kesalahan` });
    }
  };

  module.exports={presensIfvalid}