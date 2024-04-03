const { json } = require("body-parser");
const db = require("../config/db");
const { query } = require("express");

const formatRes = (status, data, message, res) => {
  res.status(status).json({
    kelas: {
      status: status,
      dataUser: data,
      message: message,
    },
  });
};

const getUsers = async (req, res) => {
  try {
    const queryGet = 'SELECT * FROM user_mahasiswa';
    const result = await new Promise((resolve, reject) => {
  
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    formatRes(200, result, "berhasil get users", res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan"});
}
};

const addUsers = async (req, res) => {
  try {
    const {
      nim,
      kode_prodi,
      tahun_angkatan,
      nama,
    } = req.body;
    const query = `INSERT INTO user_mahasiswa (nim, kode_prodi, tahun_angkatan, nama) VALUES (?, ?, ?, ?)`;
    const values = [
        nim,
        kode_prodi,
        tahun_angkatan,
        nama,
    ];

    const result = await new Promise((resolve, reject) => {
      db.run(query, values, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });

    formatRes(200, result, "berhasil add users", res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const deleteUsers = async (req, res) => {
  try{
    const { nim } = req.params;
  console.log("id yang dihapus:", nim);

  const query = `DELETE FROM user_mahasiswa WHERE nim= '${nim}';`;

  const result = await new Promise((resolve, reject)=>{
    db.run(query,(error, result)=>{
      if(error) reject(error);
      resolve(result);
    });
  });
  formatRes(200,nim,"berhasil hapus user ",res)
  }
  catch(error){
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const editUsers = async (req, res) => {
  try {
    const { nim } = req.params;
    console.log("nim yang diedit:", nim);

    // requestnya
    const newData = req.body;

    // nampung req dari new data
    let setValues = "";

    // id ga bole di edit
    if ("nim" in newData) {
      return res.status(400).json({ message: "Dilarang mengubah ID" });
    }

    // Membuat string setValues
    for (const key in newData) {
      setValues += `${key}='${newData[key]}', `;
    }

    // Hapus koma ekstra di akhir string setValues
    setValues = setValues.slice(0, -2);

    const query = `
      UPDATE user_mahasiswa
      SET ${setValues}
      WHERE nim='${nim}'
    `;

    await new Promise((resolve, reject) => {
      db.run(query, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });

    formatRes(200, "berhasil edit", "nim"+nim, res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Terjadi kesalahan' });
  }
};


module.exports = { getUsers, addUsers, editUsers, deleteUsers };
