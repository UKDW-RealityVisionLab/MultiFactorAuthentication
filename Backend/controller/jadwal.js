const db = require("../config/db");

const getJadwal = async (req, res) => {
  try {
    const queryGet = `SELECT 
        kelas.group_kelas, 
        kelas.kode_kelas,
        jadwal.tanggal,
        jadwal.kode_jadwal,
        jadwal.kode_sesi,
        jadwal.kode_ruang, 
        kelas_sesi.sesi_start,
        kelas_sesi.sesi_end,
        mata_kuliah.nama_matakuliah
    FROM 
        kelas 
    INNER JOIN 
        jadwal ON kelas.kode_kelas = jadwal.kode_kelas 
    INNER JOIN 
        kelas_sesi ON jadwal.kode_sesi = kelas_sesi.kode_sesi
    INNER JOIN 
        mata_kuliah ON kelas.kode_matakuliah = mata_kuliah.kode_matakuliah;`;

    const result = await new Promise((resolve, reject) => {
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    res.json(result.map(result => ({
      kodeKelas: result.kode_kelas,
      mataKuliah: result.nama_matakuliah,
      grup: result.group_kelas,
      tanggal: result.tanggal,
      kodeJadwal: result.kode_jadwal,
      kodeSesi: result.kode_sesi,
      kodeRuang: result.kode_ruang,
      sesiStart: result.sesi_start,
      sesiEnd: result.sesi_end
    })));
    // res.json(result)
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const getJadwalBykodeKelas = async (req, res) => {
  try {
    let kodeKelas;
    let source;

    if (req.body.kodeKelas) {
      kodeKelas = req.body.kodeKelas;
      source = "req.body";
    } else if (req.params.kode_kelas) {
      kodeKelas = req.params.kode_kelas;
      source = "req.params";
    }

    console.log("Received kodeKelas:", kodeKelas, "from", source);

    if (!kodeKelas) {
      return res.status(400).json({ message: "kodeKelas tidak ditemukan dalam request body atau params" });
    }

    const query = `
      SELECT 
        kelas.group_kelas, 
        kelas.kode_kelas,
        jadwal.tanggal,
        jadwal.kode_jadwal,
        jadwal.kode_sesi,
        jadwal.kode_ruang, 
        kelas_sesi.sesi_start,
        kelas_sesi.sesi_end,
        mata_kuliah.nama_matakuliah
      FROM 
        kelas 
      INNER JOIN 
        jadwal ON kelas.kode_kelas = jadwal.kode_kelas 
      INNER JOIN 
        kelas_sesi ON jadwal.kode_sesi = kelas_sesi.kode_sesi
      INNER JOIN 
        mata_kuliah ON kelas.kode_matakuliah = mata_kuliah.kode_matakuliah
      WHERE 
        kelas.kode_kelas = ?;
    `;

    const result = await new Promise((resolve, reject) => {
      db.all(query, [kodeKelas], (error, results) => {
        if (error) {
          console.error("Database error:", error);
          return reject(error);
        }
        resolve(results);
      });
    });

    if (!result.length) {
      return res.status(404).json({ message: "Tidak ada jadwal ditemukan untuk kode_kelas ini" });
    }

    const formattedResult = result.map(item => ({
      kodeKelas: item.kode_kelas,
      mataKuliah: `${item.nama_matakuliah} ${item.group_kelas}`,
      grup: item.group_kelas,
      tanggal: item.tanggal,
      jadwal: item.kode_jadwal,
      sesi:  `sesi ${item.kode_sesi} ${item.sesi_start} - ${item.sesi_end} `,
      ruang: item.kode_ruang,
      sesiStart: item.sesi_start,
      sesiEnd: item.sesi_end
    }));

    res.json(formattedResult);
  } catch (error) {
    console.error("Server error:", error);
    res.status(500).json({ message: "Terjadi kesalahan" });
  }
};



const getByKodeJadwal = async (req, res) => {
  try {
    const { kode_jadwal } = req.params;
    const queryGet = `SELECT *
                      FROM jadwal
                      WHERE kode_jadwal = '${kode_jadwal}';`;
    const result = await new Promise((resolve, reject) => {
      db.all(queryGet, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
   res.json(result)
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const addJadwal = async (req, res) => {
  try {
    const { kode_jadwal, kode_kelas, kode_ruang, kode_sesi, tanggal } =
      req.body;

    const query = `INSERT INTO jadwal (kode_jadwal, kode_kelas, kode_ruang, kode_sesi, tanggal) VALUES (?,?, ?, ?, ?)`;

    const values = [kode_jadwal, kode_kelas, kode_ruang, kode_sesi, tanggal];

    const result = await new Promise((resolve, reject) => {
      db.run(query, values, (err, result) => {
        if (err) reject(err);
        resolve(result);
      });
    });
    res.json({message:"success add jadwal"})
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const deleteJadwal = async (req, res) => {
  try {
    const { kode_jadwal } = req.params;
    console.log("jadwal yang dihapus:", kode_jadwal);

    const query = `DELETE FROM jadwal WHERE kode_jadwal= '${kode_jadwal}';`;

    await new Promise((resolve, reject) => {
      db.run(query, (error, result) => {
        if (error) reject(error);
        resolve(result);
      });
    });
    res.json({message:` success delete ${kode_jadwal}`})
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

const editJadwal = async (req, res) => {
  try {
    const { kode_jadwal } = req.params;
    console.log("jadwal yang diedit:", kode_jadwal);

    // requestnya
    const newData = req.body;

    // nampung req dari new data
    let setValues = "";

    // id ga bole di edit
    if ("kode_jadwal" in newData) {
      return res.status(400).json({ message: "Dilarang mengubah primary key" });
    }

    // Membuat string setValues
    for (const key in newData) {
      setValues += `${key}='${newData[key]}', `;
    }

    // Hapus koma ekstra di akhir string setValues
    setValues = setValues.slice(0, -2);

    const query = `
      UPDATE jadwal
      SET ${setValues}
      WHERE kode_jadwal='${kode_jadwal}'
    `;

    console.log("Generated SQL query:", query); // Log the generated SQL query

    await new Promise((resolve, reject) => {
      db.run(query, (err, result) => {
        if (err) {
          console.error("Database error:", err); // Log the database error
          reject(err);
        }
        resolve(result);
      });
    });

    res.json({message:`success edit ${kode_jadwal}`})
  } catch (error) {
    console.error("Server error:", error); 
    return res.status(500).json({ message: "Terjadi kesalahan" });
  }
};

module.exports = {
  getJadwal,
  addJadwal,
  editJadwal,
  deleteJadwal,
  getByKodeJadwal,
  getJadwalBykodeKelas,
};
