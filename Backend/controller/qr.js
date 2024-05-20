const db = require("../config/db");
const QRCode = require('qrcode');
let currentTime = new Date();
let expiryTime = new Date(currentTime.getTime() + 5 * 60000); // Calculate expiry time
let dataQr;
const crypto = require('crypto');

const generateQr = (req, res) => {

    if (typeof generateQr.isQRGenerated === 'undefined') {
        generateQr.isQRGenerated = false;
        generateQr.QRCode = "";
    }

    if (generateQr.QRCode && (generateQr.isQRGenerated && new Date() < expiryTime)) {
        console.log(new Date() < expiryTime);
        res.status(500).json({messege:"QR code already existed"});
    } else {
        currentTime = new Date();
        expiryTime = new Date(currentTime.getTime() + 0.5 * 60000);
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
                res.json(500).json("failed generate qr");
            } else {
                // Save data and send response indicating QR code generated successfully
                dataQr = data;
                generateQr.QRCode = url;
                res.json(url);
            }
        });
        generateQr.isQRGenerated = true;
    }


};

const validation = async (req, res) => {
    const timeReceive = new Date().toLocaleString();
    if (!req.body || req.body == null) {
        res.json({messege:'no request'});
        return;
    }

    let dataReceived = JSON.stringify(req.body[0].data + req.body[1].data);
    let dataQrValid = JSON.stringify(dataQr[0].data + dataQr[1].data);

    if (timeReceive > expiryTime.toLocaleString()) {
        res.status(500).json("Qr is expired");
    } else if (dataReceived !== dataQrValid) {
        res.status(500).json('Qr is not matching');
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
            res.json({message:`${user} berhasil presensi`});
        } else {
            // Send response indicating user not found
            res.status(500).json({messege:`${user} is not found`});
        }
    }
    catch (error) {
        console.error(error);
        // Send response indicating an error occurred
        res.status(500).json({messege:`terjadi kesalahan`});
    }
};

module.exports = { generateQr, validation, presensIfvalid };