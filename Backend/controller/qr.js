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
            dataQr=data
            return dataQr
            // data(dataQr)
        }
    });
};
// const data= (data)=>{
//     return data
// }

const validation = (req, res) => {
    const timeReceive = new Date().toLocaleString();

    let dataReceived = JSON.stringify(req.body);
    dataQrValid= JSON.stringify(dataQr)


    if (timeReceive > expiryTime.toLocaleString()) {
        formatRes(400, null, 'QR code is not valid - QR code expired', res);
    }else if(dataReceived!=dataQrValid){
        formatRes(400,"Data received: "+ dataReceived +" valid data: "+dataQrValid ,"QR IS NOT MATCH",res)
    }
     else {
        formatRes(200," GENERATE: " + currentTime.toLocaleString()+ " REQ RECEIVED AT: "+ timeReceive  +" EXPIRED:"+ expiryTime.toLocaleString()+ "data req: "+ dataReceived, 'QR code is valid', res);
    }
}

module.exports = { generateQr, validation };
