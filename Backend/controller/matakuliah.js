const { json } = require("body-parser");
const db = require("../config/db");
const { query } = require("express");

const formatRes = (status, data, message, res) => {
  res.status(status).json({
    matakuliah : {
      status: status,
      dataMataKuliah: data,
      message: message,
    },
  });
};

const getMataKuliah = async (req, res) => {
  try {
    const queryGet = 'SELECT * FROM mata_kuliah';
    const result = await new Promise((resolve, reject) => {
  
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    formatRes(200, result, "berhasil get mata kuliah", res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan"});
}
};

const addMataKuliah = async (req, res) => {
  try {
    const {
      kode_matakuliah,
      nama_matakuliah,
      sks,
      harga,
      is_praktikum,
      minimal_sks,
      tanggal_input
    } = req.body;
    const query = `INSERT INTO mata_kuliah (kode_matakuliah, nama_matakuliah, sks, harga, is_praktikum, minimal_sks, tanggal_input) VALUES (?, ?, ?, ?, ?,?,?)`;
    const values = [
        kode_matakuliah,
        nama_matakuliah,
        sks,
        harga,
        is_praktikum,
        minimal_sks,
        tanggal_input
    ];

    const result = await new Promise((resolve, reject) => {
      db.run(query, values, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });

    formatRes(200, result, "berhasil add mata kuliah", res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const deleteMataKuliah = async (req, res) => {
  try{
    const { id } = req.params;
  console.log("id yang dihapus:", id);

  const query = `DELETE FROM mata_kuliah WHERE kode_matakuliah= '${id}';`;

  const result = await new Promise((resolve, reject)=>{
    db.run(query,(error, result)=>{
      if(error) reject(error);
      resolve(result);
    });
  });
  formatRes(200,id,"berhasil hapus mata kuliah",res)
  }
  catch(error){
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const editMataKuliah = async (req, res) => {
  try {
    const { id } = req.params;
    console.log("id yang diedit:", id);

    // requestnya
    const newData = req.body;

    // nampung req dari new data
    let setValues = "";

    // id ga bole di edit
    if ("kode_matakuliah" in newData) {
      return res.status(400).json({ message: "Dilarang mengubah ID" });
    }

    // Membuat string setValues
    for (const key in newData) {
      setValues += `${key}='${newData[key]}', `;
    }

    // Hapus koma ekstra di akhir string setValues
    setValues = setValues.slice(0, -2);

    const query = `
      UPDATE mata_kuliah
      SET ${setValues}
      WHERE kode_matakuliah='${id}'
    `;

    await new Promise((resolve, reject) => {
      db.run(query, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });

    formatRes(200, "berhasil edit", "id"+id, res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Terjadi kesalahan' });
  }
};


module.exports = { getMataKuliah, addMataKuliah, editMataKuliah, deleteMataKuliah };
