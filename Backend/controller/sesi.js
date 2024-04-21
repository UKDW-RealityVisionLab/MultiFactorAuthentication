const { json } = require("body-parser");
const db = require("../config/db");
const { query } = require("express");

const formatRes = (status, data, message, res) => {
    res.status(status).json({
        sesi: {
            status: status,
            dataKelas: data,
            message: message,
        },
    });
};

const getSesi = async (req, res) => {
    try {
        const queryGet = 'SELECT * FROM kelas_sesi';
        const result = await new Promise((resolve, reject) => {

            db.all(queryGet, (error, result) => {
                if (error) reject(error);
                resolve(result);
            });
        });
        formatRes(200, result, "berhasil get sesi", res);
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Terjadi kesalahan"});
    }
};

module.exports= {getSesi}