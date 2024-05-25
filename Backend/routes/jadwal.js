const express = require('express');
const route = express.Router();

const controller= require('../controller/jadwal')

route.get('/', controller.getJadwal)
route.post('/', controller.addJadwal)

route.patch('/:kode_jadwal',controller.editJadwal)

route.delete('/:kode_jadwal',controller.deleteJadwal)
route.get('/:kode_jadwal',controller.getByKodeJadwal)
route.post('/jadwalPresensi/:kode_kelas',controller.getJadwalBykodeKelas)

module.exports= route