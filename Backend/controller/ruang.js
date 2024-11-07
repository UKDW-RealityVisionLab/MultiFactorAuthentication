const { query } = require("express");
const db = require("../config/db");

const getRuang = async (req, res) => {
    try {
        const queryGet = 'SELECT * FROM ruang';
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

const getRuangByid = async (req, res) => {
    const queryId = `SELECT * FROM ruang WHERE kodeRuang="${req.params.kodeRuang}"`;

    try {
        const result = await new Promise((resolve, reject) => {
            db.all(queryId, (err, rows) => {
                if (err) return reject(err);
                resolve(rows);
            });
        });
        res.json(result); 
    } catch (error) {
        res.status(500).json({ error: error.message }); 
    }
};


const getRuangByIdJadwal = async (req, res) => {
    try {
        let idJadwal;
        let source;

        if (req.body.idJadwal) {
            idJadwal = req.body.idJadwal;
            source = "req.body";
        }
        // } else if (req.params.idJadwal) {
        //     idJadwal = req.params.idJadwal;
        //     source = "req.params";
        // }

        console.log("Received idJadwal:", idJadwal, "from", source);

        const queryGet = `SELECT ruang.latitude, ruang.longitude
                FROM ruang 
                    JOIN jadwal ON ruang.kodeRuang = jadwal.kode_ruang
                        WHERE jadwal.kode_jadwal = ?;`;

        const result = await new Promise((resolve, reject) => {
            db.all(queryGet,[idJadwal], (error, result) => {
                if (error) reject(error);
                resolve(result);
            });
        });
        let respond = {};
      result.forEach(result => {
        respond= {
            latitude: result.latitude,
            longtitude: result.longitude,
        };
      });
      res.json(respond)

        if (result.length === 0) {
            return res.status(404).json({ message: "Data not found" });
        }

    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Terjadi kesalahan" });
    }
};


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

        res.json({ message: "success add ruang" });
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
        res.json({ message: `success delete ${kode_ruang}` })
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

        res.json({ message: `success edit ruang ${kode_ruang}` });
    } catch (error) {
        console.error("Server error:", error); // Log the server error
        return res.status(500).json({ message: 'Terjadi kesalahan' });
    }
};



module.exports = { getRuang, addRuang, editruang, deleteRuang, getRuangByIdJadwal, getRuangByid };
