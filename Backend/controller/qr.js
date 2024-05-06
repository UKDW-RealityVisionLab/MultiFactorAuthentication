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

const generateQr = (req, res) => {
    const { kode_jadwal } = req.params;
    
    const data = kode_jadwal;
    const timestamp = Date.now(); 
    const expiryTime = timestamp + (5 * 60 * 1000); // Menambah 5 menit ke timestamp

    const qrData = { text: data, expiry: expiryTime }; 
    QRCode.toDataURL(JSON.stringify(qrData), (err, url) => {
        if (err) {
            console.error(err);
            formatRes(500, null, 'Error generating QR code', res);
        } else {
            formatRes(200, url, 'QR code generated successfully', res);
        }
    });
   

};


module.exports = { generateQr };
