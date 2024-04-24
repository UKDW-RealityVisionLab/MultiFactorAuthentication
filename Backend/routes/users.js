const express = require('express');
const route = express.Router();

const controller= require('../controller/users')

route.get('/',controller.getUsers)

route.post('/',controller.addUsers )

route.patch('/:nim',controller.editUsers)

route.delete('/:nim',controller.deleteUsers)

route.get('/:nim',controller.getByIdMahasiswa)

module.exports= route