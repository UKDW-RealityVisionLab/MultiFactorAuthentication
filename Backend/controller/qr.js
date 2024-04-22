const { json } = require("body-parser");
const db = require("../config/db");
const { query } = require("express");

const formatRes = (status, data, message, res) => {
  res.status(status).json({
    user: {
      status: status,
      dataQR: data,
      message: message,
    },
  });
};

const getQR = async (req, res) => {
  try {
    const queryGet = 'SELECT * FROM qr';
    const result = await new Promise((resolve, reject) => {
  
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    formatRes(200, result, "berhasil get QR", res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan"});
}
};

const addQR = async (req, res) => {
  try {
    const {
      nidn,
      kode_prodi,
      nama,
    } = req.body;
    const query = `INSERT INTO qr (nidn, kode_prodi,nama) VALUES (?, ?, ?)`;
    const values = [
        nidn,
        kode_prodi,
        nama,
    ];

    const result = await new Promise((resolve, reject) => {
      db.run(query, values, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });

    formatRes(200, result, "berhasil add QR", res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const deleteQR = async (req, res) => {
  try{
    const { nidn } = req.params;
  console.log("qr yang dihapus:", nidn);

  const query = `DELETE FROM QR WHERE nidn= '${nidn}';`;

  await new Promise((resolve, reject)=>{
    db.run(query,(error, result)=>{
      if(error) reject(error);
      resolve(result);
    });
  });
  formatRes(200,nidn,"berhasil hapus user ",res)
  }
  catch(error){
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const editQR = async (req, res) => {
  try {
    const { nidn } = req.params;
    console.log("nim yang diedit:", nidn);

    // requestnya
    const newData = req.body;

    // nampung req dari new data
    let setValues = "";

    // id ga bole di edit
    if ("nidn" in newData) {
      return res.status(400).json({ message: "Dilarang mengubah NIDN" });
    }

    // Membuat string setValues
    for (const key in newData) {
      setValues += `${key}='${newData[key]}', `;
    }

    // Hapus koma ekstra di akhir string setValues
    setValues = setValues.slice(0, -2);

    const query = `
      UPDATE user_dosen
      SET ${setValues}
      WHERE nidn='${nidn}'
    `;

    await new Promise((resolve, reject) => {
      db.run(query, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });

    formatRes(200, "berhasil edit", "nidn"+nidn, res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Terjadi kesalahan' });
  }
};


module.exports = { getQR, addQR, editQR, deleteQR };
