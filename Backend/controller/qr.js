const formatRes = (status, data, message, res) => {
    res.status(status).json({
        presensi: {
            status: status,
            data: data,
            message: message,
        },
    });
};


const { json } = require('body-parser');
const QRCode = require('qrcode');
let currentTime = new Date();
let expiryTime = new Date(currentTime.getTime() + 5 * 60000); // Calculate expiry time
var dataQr;
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
            formatRes(200, url, 'QR code generated successfully', res);
            dataQr = data
            return dataQr
            // data(dataQr)
        }
    });
};


const validation = async (req, res) => {
    const timeReceive = new Date().toLocaleString();
    //cek jika ga req empty
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
        formatRes(200, " GENERATE: " + currentTime.toLocaleString() + " REQ RECEIVED AT: " + timeReceive + " EXPIRED:" + expiryTime.toLocaleString() + "data req: " + dataReceived, 'QR code is valid', res);
    //     try {
    //         const user = dataReceived[2].user; // Extract the ID from the request parameters
    //         const queryUpdate = `UPDATE presensi SET hadir = hadir WHERE nama = '${user}'`;
    //         const result = await new Promise((resolve, reject) => {
    //             db.run(queryUpdate, [user], function (error) {
    //                 if (error) reject(error);
    //                 resolve({ affectedRows: this.changes }); // Return the number of affected rows
    //             });
    //         });

    //         if (result.affectedRows > 0) {
    //             formatRes(200, result, "Data berhasil diupdate", res);
    //         } else {
    //             return res.status(404).json({ message: "Data tidak ditemukan" });
    //         }
    //     } catch (error) {
    //         console.error(error);
    //         return res.status(500).json({ message: "Terjadi kesalahan" });
    //     }
    // }
}
}

module.exports = { generateQr, validation };
