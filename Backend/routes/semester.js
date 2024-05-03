const express = require('express');
const route = express.Router();

const controller= require('../controller/semester')

route.get('/',controller.getSemester);

route.post('/',controller.addSemester );

route.delete('/:kode_semester',controller.deleteSemester);

route.patch('/:kode_semester',controller.editSemester);


route.get('/:kode_semester',controller.getByIdSemester);


module.exports= route