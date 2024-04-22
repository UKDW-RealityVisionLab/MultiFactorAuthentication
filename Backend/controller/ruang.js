const { json } = require("body-parser");
const db = require("../config/db");
const { query } = require("express");

const formatRes = (status, data, message, res) => {
    res.status(status).json({
        ruang: {
            status: status,
            dataRuang: data,
            message: message,
        },
    });
};

const getRuang = async (req, res) => {
    try {
        const queryGet = 'SELECT * FROM ruang';
        const result = await new Promise((resolve, reject) => {

            db.all(queryGet, (error, result) => {
                if (error) reject(error);
                resolve(result);
            });
        });
        formatRes(200, result, "berhasil get ruang", res);
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Terjadi kesalahan" });
    }
};

const getByKodeRuang = async (req, res) => {
    try {
        const { kode_ruang } = req.params;
        const queryGet = `SELECT *
                      FROM ruang
                      WHERE kode_ruang = '${kode_ruang}';`;
        const result = await new Promise((resolve, reject) => {

            db.all(queryGet, (error, result) => {
                if (error) reject(error);
                resolve(result);
            });
        });
        formatRes(200, result, "berhasil get ruang", res);
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Terjadi kesalahan" });
    }
}

const addRuang = async (req, res) => {
    try {
        const {
            kode_matakuliah,
            nama_matakuliah,
            sks,
            harga,
            is_praktikum,
            minimal_sks,
            tanggal_input
        } = req.body;
        const query = `INSERT INTO mata_kuliah (kode_matakuliah, nama_matakuliah, sks, harga, is_praktikum, minimal_sks, tanggal_input) VALUES (?, ?, ?, ?, ?,?,?)`;
        const values = [
            kode_matakuliah,
            nama_matakuliah,
            sks,
            harga,
            is_praktikum,
            minimal_sks,
            tanggal_input
        ];

        const result = await new Promise((resolve, reject) => {
            db.run(query, values, (err, result) => {
                if (err) reject(err);
                resolve(result);
            });
        });

        formatRes(200, result, "berhasil add mata kuliah", res);
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Terjadi kesalahan" });
    }
};

const deleteRuang = async (req, res) => {
    try {
        const { kode_ruang } = req.params;
        console.log("ruang yang dihapus:", kode_ruang);

        const query = `DELETE FROM ruang WHERE kode_ruang= '${kode_ruang}';`;

        await new Promise((resolve, reject) => {
            db.run(query, (error, result) => {
                if (error) reject(error);
                resolve(result);
            });
        });
        formatRes(200, id, "berhasil hapus ruang", res)
    }
    catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Terjadi kesalahan" });
    }
};

const editruang = async (req, res) => {
    try {
        const { kode_ruang } = req.params;
        console.log("ruang yang diedit:", kode_ruang);

        // requestnya
        const newData = req.body;

        // nampung req dari new data
        let setValues = "";

        // id ga bole di edit
        if ("kode_ruang" in newData) {
            return res.status(400).json({ message: "Dilarang mengubah primary key" });
        }

        // Membuat string setValues
        for (const key in newData) {
            setValues += `${key}='${newData[key]}', `;
        }

        // Hapus koma ekstra di akhir string setValues
        setValues = setValues.slice(0, -2);

        const query = `
      UPDATE mata_kuliah
      SET ${setValues}
      WHERE kode_matakuliah='${id}'
    `;

        await new Promise((resolve, reject) => {
            db.run(query, (err, result) => {
                if (err) reject(err);
                resolve(result);
            });
        });

        formatRes(200, "berhasil edit", "id" + id, res);
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: 'Terjadi kesalahan' });
    }
};


module.exports = { getRuang, addRuang, editruang, deleteRuang, getByKodeRuang };
