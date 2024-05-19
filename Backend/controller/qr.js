const formatRes = (status, data, message, res) => {
    res.status(status).json({
        presensi: {
            status: status,
            data: data,
            message: message,
        },
    });
};

const db = require("../config/db");
const QRCode = require('qrcode');
let currentTime = new Date();
let expiryTime = new Date(currentTime.getTime() + 5 * 60000); // Calculate expiry time
let dataQr;
const crypto = require('crypto');

const generateQr = (req, res) => {
    const { kode_jadwal } = req.params;
    const created_at = currentTime.toLocaleString();
    const expired_at = expiryTime.toLocaleString();

    console.log('generate:', created_at);
    console.log('expire:', expired_at);

    const data = [
        { data: `${kode_jadwal} ${created_at}` },
        { data: `${kode_jadwal} ${expired_at}` }
    ];

    QRCode.toDataURL(data, { scale: 10 }, (err, url) => {
        console.log('data generate is:', data);
        if (err) {
            console.error(err);
            formatRes(500, null, 'Error generating QR code', res);
        } else {
            // Save data and send response indicating QR code generated successfully
            dataQr = data;
            formatRes(200, url, 'QR code generated successfully', res);
        }
    });
};

const validation = async (req, res) => {
    const timeReceive = new Date().toLocaleString();
    if (!req.body || req.body == null) {
        formatRes(400, null, 'Request body is missing or empty', res);
        return;
    }

    let dataReceived = JSON.stringify(req.body[0].data + req.body[1].data);
    let dataQrValid = JSON.stringify(dataQr[0].data + dataQr[1].data);

    if (timeReceive > expiryTime.toLocaleString()) {
        formatRes(400, null, 'QR code is not valid - QR code expired', res);
    } else if (dataReceived !== dataQrValid) {
        formatRes(400, "Data received: " + dataReceived + " valid data: " + dataQrValid, "QR IS NOT MATCH", res);
    } else {
        // Valid QR code, continue processing
        await presensIfvalid(req, res);
    }
};

const presensIfvalid = async (req, res) => {
    try {
        const user = req.body[2].user;
        const queryUpdate = `UPDATE presensi SET hadir = 'hadir' WHERE nama = '${user}'`;
        const result = await new Promise((resolve, reject) => {
            db.run(queryUpdate, function (error) {
                if (error) reject(error);
                resolve({ affectedRows: this.changes });
            });
        });

        if (result.affectedRows > 0) {
            // Send response indicating data successfully updated
            formatRes(200, result, "Data berhasil diupdate", res);
        } else {
            // Send response indicating user not found
            formatRes(500, result, "mahasiswa not found", res);
        }
    }
    catch (error) {
        console.error(error);
        // Send response indicating an error occurred
        formatRes(500, null, "Terjadi kesalahan", res);
    }
};

module.exports = { generateQr, validation, presensIfvalid };
