const QRCode = require('qrcode');
let currentTime = new Date();
let expiryTime = new Date(currentTime.getTime() + 5 * 60000); // Calculate expiry time
let dataQr;
let kodeJadwal;

const generateQr = (req, res) => {

    if (typeof generateQr.isQRGenerated === 'undefined') {
        generateQr.isQRGenerated = false;
        generateQr.QRCode = "";
    }

    if (generateQr.QRCode && (generateQr.isQRGenerated && new Date() < expiryTime)) {
        console.log(new Date() < expiryTime);
        res.status(500).json({ messege: "QR code already existed" });
    } else {
        currentTime = new Date();
        expiryTime = new Date(currentTime.getTime() + 5 * 60000);
        const { kode_jadwal } = req.params;
        const created_at = currentTime.toLocaleString();
        const expired_at = expiryTime.toLocaleString();

        console.log('generate:', created_at);
        console.log('expire:', expired_at);
        const data= `${kode_jadwal} ${created_at}`

        QRCode.toDataURL(data, { scale: 8 }, (err, url) => {
            console.log('data generate is:', data);
            if (err) {
                console.error(err);
                res.json(500).json("failed generate qr");
            } else {
                // Save data and send response indicating QR code generated successfully
                dataQr = data;
                kodeJadwal= kode_jadwal;
                console.log("generate kode jadwal:", kodeJadwal)
                generateQr.QRCode = url;
                res.json(url);
            }
        });
        generateQr.isQRGenerated = true;
    }
};

const validation = async (req, res) => {
    const timeReceive = new Date().toLocaleString();
    if (!req.body || req.body.length < 2 || !req.body) {
        res.status(400).json({ message: 'Invalid request body' });
        return;
    }

    let dataReceived;
    let dataQrReceive;
    let receivedKodeJadwal;
    
    try {
        dataReceived = req.body.qrCodeData + req.body.idJadwal;
        dataQrReceive = req.body.qrCodeData;
        receivedKodeJadwal = req.body.idJadwal;
    } catch (error) {
        res.status(400).json({ message: 'Invalid request body structure' });
        return;
    }

    if (timeReceive > expiryTime.toLocaleString()) {
        res.status(500).json("QR is expired");
    } else if (dataQrReceive !== dataQr || receivedKodeJadwal != kodeJadwal ) {
        res.status(500).json('QR is not matching');
        console.log(`validasi kode jadwal yang diterima: ${receivedKodeJadwal}`);
        console.log("validation kode jadwal:", kodeJadwal)
        console.log(`validasi dataQr data yang diterima: ${dataQrReceive}`);
        console.log(`validasi dataQR: ${dataQr}`);
    } else {
        res.json({ qrCodeData: "verify qrcode berhasil" });
    }
};
module.exports = { generateQr, validation};