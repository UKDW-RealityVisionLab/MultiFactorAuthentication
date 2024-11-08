const express = require('express');
const route = express.Router();

const controller= require('../controller/ruang')

route.get('/', controller.getRuang)
route.get('/getEdit/:kodeRuang', controller.getRuangByid)

route.post('/', controller.addRuang)

route.patch('/:kode_ruang',controller.editruang)

route.delete('/:kode_ruang',controller.deleteRuang)
route.post('/selectRuang',controller.getRuangByIdJadwal)

module.exports= route