const { json } = require("body-parser");
const db = require("../config/db");

const formatRes= (status, data, message,res)=>{
  res.status(status).json({
    kelas:{
      status:status,
      dataKelas: data,
      message:message
    }
  })
}

const getKelas = (req, res) => {
  const queryGet = "SELECT * FROM kelas"
  db.query(queryGet, (error,result)=>{
    try{
      formatRes(200,result,"berhasil get kelas",res)
    }catch{
      res.json({
        message:"gagal get api kelas"
      })
    }
  });
};

const addKelas = (req, res) => {
  const {kode_kelas, kode_matakuliah, group_kelas, kode_semester, kode_dosen}= req.body
  const query= `INSERT INTO kelas (kode_kelas, kode_matakuliah, group_kelas, kode_semester, kode_dosen) VALUES ('${kode_kelas}', '${kode_matakuliah}', '${group_kelas}', '${kode_semester}','${kode_dosen}')`;

  db.query(query, (err, result) => {
    if (err) {
      res.status(500).json({ message: 'Gagal menambahkan data kelas' });
      throw err;
    }
    formatRes(200,result,"berhasil add kelas",res)
  });
};

const editKelas = (req, res) => {
  const { id } = req.params;
  console.log("id yang diedit:", id);

  // requestnya
  const newData = req.body;

  // nampung req dari new data
  let setValues = '';

  // id ga bole di edit
  if ('kode_kelas' in newData) {
    return res.status(400).json({ message: 'Dilarang mengubah ID' });
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

  db.query(query, (err, result) => {
    if (err) {
      res.status(500).json({ message: 'Gagal mengedit data kelas' });
      throw err;
    }
    formatRes(200,"berhasil edit","berhasil add kelas",res)
  });
};



const deleteKelas = (req, res) => {
  const { id } = req.params;
  console.log("id yang dihapus:", id);

  const query= `DELETE FROM kelas WHERE kode_kelas= '${id}';`
  
  db.query(query, (err, result) => {
    if (err) {
      res.status(500).json({ message: 'Gagal menghapus data kelas' });
      throw err;
    }
    formatRes(200,id,"berhasil hapus kelas",res)
  });
};
module.exports = { getKelas, addKelas, editKelas, deleteKelas };
