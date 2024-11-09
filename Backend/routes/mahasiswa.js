const express = require('express');
const route = express.Router();

const controller= require('../controller/mahasiswa')

route.get('/',controller.getMahasiswa)

route.post('/',controller.addMahasiswa )

route.patch('/:nim',controller.editMahasiswa)

route.delete('/:nim',controller.deleteMahasiswa)

route.get('/:nim',controller.getByIdMahasiswa)

module.exports= route