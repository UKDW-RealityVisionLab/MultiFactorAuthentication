
const express = require('express');
const route = express.Router();

const controller= require('../controller/qr')

route.get('/', controller.generateQr)

module.exports= route