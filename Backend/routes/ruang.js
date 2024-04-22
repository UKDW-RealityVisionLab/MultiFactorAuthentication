const express = require('express');
const route = express.Router();

const controller= require('../controller/ruang')

route.get('/', controller.getRuang)

// route.post('/', MataKuliahController.addMataKuliah)

// route.patch('/:kode_ruang',MataKuliahController.editMataKuliah)

route.delete('/:kode_ruang',controller.deleteRuang)
route.get('/:kode_ruang',controller.getByKodeRuang)

module.exports= route