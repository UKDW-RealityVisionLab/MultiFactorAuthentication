const express = require('express');
const route = express.Router();

const controller= require('../controller/dosen')

route.get('/',controller.getDosen)

route.post('/',controller.addDosen )

route.patch('/:nidn',controller.editDosen)

route.delete('/:nidn',controller.deleteDosen)

module.exports= route