const { json } = require("body-parser");
const db = require("../config/db");
const { query } = require("express");

const formatRes = (status, data, message, res) => {
  res.status(status).json({
    kelas: {
      status: status,
      dataKelas: data,
      message: message,
    },
  });
};

const getKelas = async (req, res) => {
  try {
    const queryGet = 'SELECT * FROM kelas';
    const result = await new Promise((resolve, reject) => {
  
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    formatRes(200, result, "berhasil get kelas", res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const addKelas = async (req, res) => {
  try {
    const {
      kode_kelas,
      kode_matakuliah,
      group_kelas,
      kode_semester,
      kode_dosen,
    } = req.body;
    const query = `INSERT INTO kelas (kode_kelas, kode_matakuliah, group_kelas, kode_semester, kode_dosen) VALUES (?, ?, ?, ?, ?)`;
    const values = [
      kode_kelas,
      kode_matakuliah,
      group_kelas,
      kode_semester,
      kode_dosen,
    ];

    const result = await new Promise((resolve, reject) => {
      db.run(query, values, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });

    formatRes(200, result, "berhasil add kelas", res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const deleteKelas = async (req, res) => {
  try{
    const { id } = req.params;
  console.log("id yang dihapus:", id);

  const query = `DELETE FROM kelas WHERE kode_kelas= '${id}';`;

  const result = await new Promise((resolve, reject)=>{
    db.run(query,(error, result)=>{
      if(error) reject(error);
      resolve(result);
    });
  });
  formatRes(200,id,"berhasil hapus kelas",res)
  }
  catch(error){
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};


const editKelas = async (req, res) => {
  try {
    const { id } = req.params;
    console.log("id yang diedit:", id);

    // requestnya
    const newData = req.body;

    // nampung req dari new data
    let setValues = "";

    // id ga bole di edit
    if ("kode_kelas" in newData) {
      return res.status(400).json({ message: "Dilarang mengubah ID" });
    }

    // Membuat string setValues
    for (const key in newData) {
      setValues += `${key}='${newData[key]}', `;
    }

    // Hapus koma ekstra di akhir string setValues
    setValues = setValues.slice(0, -2);

    const query = `
      UPDATE kelas
      SET ${setValues}
      WHERE kode_kelas='${id}'
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


module.exports = { getKelas, addKelas, editKelas, deleteKelas };
