const express = require('express');
const route = express.Router();

const presensiController= require('../controller/presensi.js')

route.get('/:kode_jadwal', presensiController.dataPresensi)

route.post('/', presensiController.insertPresensi)
route.post('/getProfile/:email', presensiController.getProfile)
route.post('/cekStatusPresensi', presensiController.cekStatusPresensi)
route.patch('/:id',presensiController.updatePresensi)

route.delete('/:id',presensiController.deletePresensi)

module.exports= route