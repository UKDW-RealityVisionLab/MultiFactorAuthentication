const express = require('express');
const route = express.Router();

const kelasController= require('../controller/kelas.js')

route.get('/', kelasController.getKelas)

route.post('/', kelasController.addKelas)

module.exports= route