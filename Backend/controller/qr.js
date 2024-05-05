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
    const data = 'Hello, World!';
    const timestamp = Date.now(); // Mengambil timestamp saat ini
    const expiryTime = timestamp + (5 * 60 * 1000); // Menambah 5 menit ke timestamp

    const qrData = { text: data, expiry: expiryTime }; // Menyimpan data QR code dan waktu kadaluarsa
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
