const express = require('express');
const route = express.Router();
const QR_Controller= require('../controller/qr.js')

route.get('/', QR_Controller.getQR)

route.post('/', QR_Controller.addQR)

route.patch('/:id',QR_Controller.editQR)

route.delete('/:id',QR_Controller.deleteQR)
module.exports= route