const db = require("../config/db");

const getSemester = async (req, res) => {
  try {
    const queryGet = 'SELECT * FROM semester';
    const result = await new Promise((resolve, reject) => {
  
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    res.json(result);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan"});
}
};
const addSemester = async (req, res) => {
    try {
      const {
        kode_semester,
        tahun_ajaran,
        tanggal_mulai,
        tanggal_selesai,
      } = req.body;
      const query = `INSERT INTO semester (kode_semester, tahun_ajaran, tanggal_mulai, tanggal_selesai) VALUES (?, ?, ?, ?)`;
      const values = [
          kode_semester,
          tahun_ajaran,
          tanggal_mulai,
          tanggal_selesai,

      ];
  
      const result = await new Promise((resolve, reject) => {
        db.run(query, values, (err, result) => {
          if (err) reject(err);
          resolve(result);
        });
      });
  
      res.json({message:'success add semester'});
    } catch (error) {
      console.error(error);
      return res.status(500).json({ message: "Terjadi kesalahan" });
    }
  };
  
  const deleteSemester = async (req, res) => {
    try{
      const { kode_semester } = req.params;
    console.log("id yang dihapus:", kode_semester);
  
    const query = `DELETE FROM semester WHERE kode_semester= '${kode_semester}';`;
  
   await new Promise((resolve, reject)=>{
      db.run(query,(error, result)=>{
        if(error) reject(error);
        resolve(result);
      });
    });
    res.json({messege:`success delete ${kode_semester}`})
    }
    catch(error){
      console.error(error);
      return res.status(500).json({ message: "Terjadi kesalahan" });
    }
  };
  const editSemester = async (req, res) => {
    try {
      const { kode_semester } = req.params;
      console.log("Kode Semester yang diedit:", kode_semester);
  
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
        UPDATE semester
        SET ${setValues}
        WHERE kode_semester='${kode_semester}'
      `;
  
      await new Promise((resolve, reject) => {
        db.run(query, (err, result) => {
          if (err) reject(err);
          resolve(result);
        });
      });
  
      res.json({messege:`success edit ${kode_semester}`})
    } catch (error) {
      console.error(error);
      return res.status(500).json({ message: 'Terjadi kesalahan' });
    }
  };
  const getByIdSemester =async (req, res) => {
    try {
      const {kode_semester} = req.params;
      const queryGet = `SELECT *
                        FROM semester
                        WHERE kode_semester = '${kode_semester}';`;
      const result = await new Promise((resolve, reject) => {
  
        db.all(queryGet, (error, result) => {
          if (error) reject(error);
          resolve(result);
        });
      });
      res.json(result);
    } catch (error) {
      console.error(error);
      return res.status(500).json({message: "Terjadi kesalahan"});
    }
  };


module.exports = { getSemester, addSemester, deleteSemester, editSemester, getByIdSemester };
