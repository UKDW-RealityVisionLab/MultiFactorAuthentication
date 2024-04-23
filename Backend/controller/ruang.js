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
                      WHERE kodeRuang = '${kode_ruang}';`;
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
            kodeRuang, nama, latitude, longitude // Corrected the variable name from 'longitute' to 'longitude'
        } = req.body;

        const query = `INSERT INTO ruang (kodeRuang, nama, latitude, longitude) VALUES (?, ?, ?, ?)`;
        // Removed unnecessary placeholders from the query string

        const values = [
            kodeRuang, nama, latitude, longitude
        ];

        const result = await new Promise((resolve, reject) => {
            db.run(query, values, (err, result) => {
                if (err) reject(err);
                resolve(result);
            });
        });

        formatRes(200, result, "berhasil add ruang", res);
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Terjadi kesalahan" });
    }
};

const deleteRuang = async (req, res) => {
    try {
        const { kode_ruang } = req.params;
        console.log("ruang yang dihapus:", kode_ruang);

        const query = `DELETE FROM ruang WHERE kodeRuang= '${kode_ruang}';`;

        await new Promise((resolve, reject) => {
            db.run(query, (error, result) => {
                if (error) reject(error);
                resolve(result);
            });
        });
        formatRes(200, kode_ruang, "berhasil hapus ruang", res)
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
        if ("kodeRuang" in newData) {
            return res.status(400).json({ message: "Dilarang mengubah primary key" });
        }

        // Membuat string setValues
        for (const key in newData) {
            setValues += `${key}='${newData[key]}', `;
        }

        // Hapus koma ekstra di akhir string setValues
        setValues = setValues.slice(0, -2);

        const query = `
      UPDATE ruang
      SET ${setValues}
      WHERE kodeRuang='${kode_ruang}'
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

        formatRes(200, "berhasil edit", "ruang" + kode_ruang, res);
    } catch (error) {
        console.error("Server error:", error); // Log the server error
        return res.status(500).json({ message: 'Terjadi kesalahan' });
    }
};



module.exports = { getRuang, addRuang, editruang, deleteRuang, getByKodeRuang };
