const db = require("../config/db");

const getSesi = async (req, res) => {
    try {
        const queryGet = 'SELECT * FROM kelas_sesi';
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

module.exports= {getSesi}