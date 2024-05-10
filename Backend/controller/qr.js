const formatRes = (status, data, message, res) => {
    res.status(status).json({
        presensi: {
            status: status,
            data: data,
            message: message,
        },
    });
};


const QRCode = require('qrcode');
let currentTime = new Date();
let expiryTime = new Date(currentTime.getTime() + 0.5 * 60000); // Calculate expiry time

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
        }
    });
};


const validation = (req, res) => {
    const timeReceive = new Date().toLocaleString();

    if (timeReceive > expiryTime.toLocaleString()) {
        formatRes(400, null, 'QR code is not valid - QR code expired', res);
    } else {
        formatRes(200," GENERATE: " + currentTime.toLocaleString()+ "REQ RECEIVED AT: "+ timeReceive  +"EXPIRED:"+ expiryTime.toLocaleString(), 'QR code is valid', res);
    }
}

module.exports = { generateQr, validation };
