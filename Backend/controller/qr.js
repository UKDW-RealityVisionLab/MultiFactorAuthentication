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
      time_remain,
      time_start,
      time_end,
      qr_code,
      qr_value,
      kode_qr
    } = req.body;
    const query = `INSERT INTO qr (time_remain,time_start,time_end,qr_code,qr_value,kode_qr) VALUES (?, ?, ?,?,?,?)`;
    const values = [
      time_remain,
      time_start,
      time_end,
      qr_code,
      qr_value,
      kode_qr
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
    const { kode } = req.params;
  console.log("qr yang dihapus:", kode);

  const query = `DELETE FROM QR WHERE kode= '${kode}';`;

  await new Promise((resolve, reject)=>{
    db.run(query,(error, result)=>{
      if(error) reject(error);
      resolve(result);
    });
  });
  formatRes(200,kode,"berhasil hapus user ",res)
  }
  catch(error){
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const editQR = async (req, res) => {
  try {
    const { kode } = req.params;
    console.log("kode yang diedit:", kode);

    // requestnya
    const newData = req.body;

    // nampung req dari new data
    let setValues = "";

    // id ga bole di edit
    if ("kode" in newData) {
      return res.status(400).json({ message: "Dilarang mengubah kode" });
    }

    // Membuat string setValues
    for (const key in newData) {
      setValues += `${key}='${newData[key]}', `;
    }

    // Hapus koma ekstra di akhir string setValues
    setValues = setValues.slice(0, -2);

    const query = `
      UPDATE QR
      SET ${setValues}
      WHERE kode='${kode}'
    `;

    await new Promise((resolve, reject) => {
      db.run(query, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });

    formatRes(200, "berhasil edit", "kode"+kode, res);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Terjadi kesalahan' });
  }
};


module.exports = { getQR, addQR, editQR, deleteQR };
