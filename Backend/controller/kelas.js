const db = require("../config/db");

const getKelas = async (req, res) => {
  try {
    const queryGet = `SELECT 
    kelas.group_kelas, 
    kelas.kode_kelas,
    semester.kode_semester,
    user_dosen.nidn,
    user_dosen.nama,
    kelas.kode_matakuliah,
    kelas.kode_dosen,
	mata_kuliah.nama_matakuliah
FROM 
    kelas 
INNER JOIN 
    semester ON kelas.kode_semester = semester.kode_semester 
INNER JOIN 
    user_dosen ON kelas.kode_dosen = user_dosen.nidn
INNER join mata_kuliah ON kelas.kode_matakuliah= mata_kuliah.kode_matakuliah;
`;
    const result = await new Promise((resolve, reject) => {
  
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    res.json(result.map(result => ({
      kodeKelas: result.kode_kelas,
      semester: result.kode_semester,
      dosen: result.nama,
      grup: result.group_kelas,
      matakuliah:result.nama_matakuliah,
      kodeMatakuliah: result.kode_matakuliah,
      kodeDosen: result.kode_dosen
    })));
    
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan"});
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

    await new Promise((resolve, reject) => {
      db.run(query, values, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });

    res.json({message:'success add kelas'})
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

  await new Promise((resolve, reject)=>{
    db.run(query,(error, result)=>{
      if(error) reject(error);
      resolve(result);
    });
  });
  res.json({message:`success delete ${id}`})
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

    res.json({
      message: `success edit ${id}`,
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Terjadi kesalahan' });
  }
};
const getByIdKelas =async (req, res) => {
  try {
    const {kode_kelas} = req.params;
    const queryGet = `SELECT 
    kelas.group_kelas, 
    kelas.kode_kelas,
    semester.kode_semester,
    user_dosen.nidn,
    user_dosen.nama,
    kelas.kode_matakuliah,
    kelas.kode_dosen,
	mata_kuliah.nama_matakuliah
FROM 
    kelas 
INNER JOIN 
    semester ON kelas.kode_semester = semester.kode_semester 
INNER JOIN 
    user_dosen ON kelas.kode_dosen = user_dosen.nidn
INNER join mata_kuliah ON kelas.kode_matakuliah= mata_kuliah.kode_matakuliah
WHERE kelas.kode_kelas= ${kode_kelas};
`;
    const result = await new Promise((resolve, reject) => {

      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    res.json(result.map(result => ({
      kodeKelas: result.kode_kelas,
      semester: result.kode_semester,
      dosen: result.nama,
      grup: result.group_kelas,
      matakuliah:result.nama_matakuliah,
      kodeDosen: result.kode_dosen,
      kodeMatakuliah: result.kode_matakuliah
    })));
  } catch (error) {
    console.error(error);
    return res.status(500).json({message: "Terjadi kesalahan"});
  }
}

module.exports = { getKelas, addKelas, editKelas, deleteKelas, getByIdKelas };
