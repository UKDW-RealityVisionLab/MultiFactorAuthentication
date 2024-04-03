const express = require('express');
const route = express.Router();

const MataKuliahController= require('../controller/matakuliah')

route.get('/', MataKuliahController.getMataKuliah)

route.post('/', MataKuliahController.addMataKuliah)

route.patch('/:id',MataKuliahController.editMataKuliah)

route.delete('/:id',MataKuliahController.deleteMataKuliah)

module.exports= route