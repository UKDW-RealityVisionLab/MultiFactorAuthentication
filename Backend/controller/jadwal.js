const db = require("../config/db");


const formatRes = (status, data, message, res) => {
    res.status(status).json({
        jadwal: {
            status: status,
            dataJadwal: data,
            message: message,
        },
    });
};

const getJadwal = async (req, res) => {
    try {
        const queryGet = 'SELECT * FROM jadwal';
        const result = await new Promise((resolve, reject) => {

            db.all(queryGet, (error, result) => {
                if (error) reject(error);
                resolve(result);
            });
        });
        formatRes(200, result, "berhasil get jadwal", res);
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Terjadi kesalahan" });
    }
};

const getByKodeJadwal= async (req, res) => {
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
        formatRes(200, result, "berhasil get jadwal  by Kode Jadwal", res);
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Terjadi kesalahan" });
    }
}

const addJadwal = async (req, res) => {
    try {
        const {
            kode_jadwal, kode_ruang, kode_sesi, tanggal
        } = req.body;

        const query = `INSERT INTO jadwal (kode_jadwal, kode_ruang, kode_sesi, tanggal) VALUES (?, ?, ?, ?)`;

        const values = [
            kode_jadwal, kode_ruang, kode_sesi, tanggal
        ];

        const result = await new Promise((resolve, reject) => {
            db.run(query, values, (err, result) => {
                if (err) reject(err);
                resolve(result);
            });
        });

        formatRes(200, result, "berhasil add jadwal", res);
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
        formatRes(200, kode_jadwal, "berhasil hapus", res)
    }
    catch (error) {
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

        formatRes(200, "berhasil edit", "jadwal" + kode_jadwal, res);
    } catch (error) {
        console.error("Server error:", error); // Log the server error
        return res.status(500).json({ message: 'Terjadi kesalahan' });
    }
};



module.exports = { getJadwal, addJadwal, editJadwal, deleteJadwal, getByKodeJadwal };
