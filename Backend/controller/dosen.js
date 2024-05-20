
const db = require("../config/db");


// const formatRes = (status, data, message, res) => {
//   res.status(status).json({
//     user: {
//       status: status,
//       dataDosen: data,
//       message: message,
//     },
//   });
// };

const getDosen = async (req, res) => {
  try {
    const queryGet = 'SELECT * FROM user_dosen';
    const result = await new Promise((resolve, reject) => {
  
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    res.json(result)
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan"});
}
};

const addDosen = async (req, res) => {
  try {
    const {
      nidn,
      kode_prodi,
      nama,
    } = req.body;
    const query = `INSERT INTO user_dosen (nidn, kode_prodi,nama) VALUES (?, ?, ?)`;
    const values = [
        nidn,
        kode_prodi,
        nama,
    ];

    await new Promise((resolve, reject) => {
      db.run(query, values, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });
    res.json({
      message:"success add dosen"
    })
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const deleteDosen = async (req, res) => {
  try{
    const { nidn } = req.params;
  console.log("nidn yang dihapus:", nidn);

  const query = `DELETE FROM user_dosen WHERE nidn= '${nidn}';`;

  await new Promise((resolve, reject)=>{
    db.run(query,(error, result)=>{
      if(error) reject(error);
      resolve(result);
    });
  });
  res.json({
    message:`succes delete ${nidn}` 
  })
  }
  catch(error){
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const editDosen = async (req, res) => {
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

    res.json({
      message:`success update ${nidn}`
    })
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Terjadi kesalahan' });
  }
};


module.exports = { getDosen, addDosen, editDosen, deleteDosen };
