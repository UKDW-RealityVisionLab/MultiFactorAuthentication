const express = require('express');
const route = express.Router();

const kelasController= require('../controller/kelas.js')

route.get('/', kelasController.getKelas)

route.post('/', kelasController.addKelas)

route.patch('/:id',kelasController.editKelas)

route.delete('/:id',kelasController.deleteKelas)

module.exports= route