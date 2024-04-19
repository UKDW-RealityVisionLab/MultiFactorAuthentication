const express = require('express');
const route = express.Router();

const presensiController= require('../controller/presensi.js')

route.get('/', presensiController.dataPresensi)

route.post('/', presensiController.insertPresensi)

route.patch('/:id',presensiController.updatePresensi)

route.delete('/:id',presensiController.deletePresensi)

route.get('/:kode_matkul',presensiController.getByIdPresensi)


module.exports= route