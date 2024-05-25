
const express = require('express');
const route = express.Router();

const controller= require('../controller/qr')

route.post('/:kode_jadwal',controller.validation)
route.get('/:kode_jadwal', controller.generateQr)


module.exports= route