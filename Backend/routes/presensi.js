const express = require('express');
const route = express.Router();

const presensiController= require('../controller/presensi.js')

route.get('/', presensiController.dataPresensi)

route.post('/', presensiController.insertPresensi)

route.patch('/:id',presensiController.updatePresensi)

route.delete('/:id',presensiController.deletePresensi)

module.exports= route