const pool = require('../config/db');
const formatRes = require('..');

const mataKuliahController = {};

mataKuliahController.getAllMataKuliah = (req, res) => {
  const query = 'SELECT * FROM mata_kuliah';
  pool.query(query, (error, results) => {
    if (error) {
      console.error('Gagal mengambil data mata kuliah:', error);
      res.status(500).json({
        mata_kuliah: {
          status: 500,
          data: null,
          message: 'Gagal mengambil data mata kuliah'
        }
      });
      return;
    }
    res.status(200).json({
      mata_kuliah: {
        status: 200,
        data: results,
        message: 'Berhasil mengambil data mata kuliah'
      }
    });
  });
};

mataKuliahController.addMataKuliah = (req, res) => {
  const { kode_matakuliah, kode_mapping, kode_kurikulum, kode_kelompok_matakuliah, kode_prodi, kode_matakuliah_praktikum, nama_matakuliah, sks, harga, is_praktikum, minimal_sks, tanggal_input } = req.body;
  const query = `INSERT INTO mata_kuliah (kode_matakuliah, kode_mapping, kode_kurikulum, kode_kelompok_matakuliah, kode_prodi, kode_matakuliah_praktikum, nama_matakuliah, sks, harga, is_praktikum, minimal_sks, tanggal_input) VALUES ('${kode_matakuliah}', '${kode_mapping}', '${kode_kurikulum}', '${kode_kelompok_matakuliah}', '${kode_prodi}', '${kode_matakuliah_praktikum}', '${nama_matakuliah}', ${sks}, ${harga}, ${is_praktikum}, ${minimal_sks}, '${tanggal_input}')`;
  
  pool.query(query, (err, result) => {
    if (err) {
      console.error('Gagal menambahkan data mata kuliah:', err);
      res.status(500).json({
        mata_kuliah: {
          status: 500,
          data: null,
          message: 'Gagal menambahkan data mata kuliah'
        }
      });
      return;
    }
    res.status(200).json({
      mata_kuliah: {
        status: 200,
        data: result,
        message: 'Berhasil menambahkan data mata kuliah'
      }
    });
  });
};

mataKuliahController.updateMataKuliah = (req, res) => {
  const { kode_matakuliah } = req.params;
  const updateFields = req.body;
  let updateQuery = "UPDATE mata_kuliah SET ";
  let firstField = true;

  for (const field in updateFields) {
    if (updateFields[field] !== undefined && updateFields[field] !== null) {
      if (!firstField) {
        updateQuery += ", ";
      }
      updateQuery += `${field} = '${updateFields[field]}'`;
      firstField = false;
    }
  }
  
  updateQuery += ` WHERE kode_matakuliah = '${kode_matakuliah}'`;

  pool.query(updateQuery, (err, result) => {
    if (err) {
      console.error('Gagal memperbarui data mata kuliah:', err);
      res.status(500).json({
        mata_kuliah: {
          status: 500,
          data: null,
          message: 'Gagal memperbarui data mata kuliah'
        }
      });
      return;
    }
    res.status(200).json({
      mata_kuliah: {
        status: 200,
        data: result,
        message: 'Berhasil memperbarui data mata kuliah'
      }
    });
  });
};

mataKuliahController.deleteMataKuliah = (req, res) => {
  const { kode_matakuliah } = req.params;
  const query = `DELETE FROM mata_kuliah WHERE kode_matakuliah = '${kode_matakuliah}'`;
  
  pool.query(query, (err, result) => {
    if (err) {
      console.error('Gagal menghapus data mata kuliah:', err);
      res.status(500).json({
        mata_kuliah: {
          status: 500,
          data: null,
          message: 'Gagal menghapus data mata kuliah'
        }
      });
      return;
    }
    res.status(200).json({
      mata_kuliah: {
        status: 200,
        data: kode_matakuliah,
        message: 'Berhasil menghapus data mata kuliah'
      }
    });
  });
};

module.exports = mataKuliahController;