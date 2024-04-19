const express = require('express');
const route = express.Router();

const MataKuliahController= require('../controller/matakuliah')

route.get('/', MataKuliahController.getMataKuliah)

route.post('/', MataKuliahController.addMataKuliah)

route.patch('/:id',MataKuliahController.editMataKuliah)

route.delete('/:id',MataKuliahController.deleteMataKuliah)
route.get('/:kode_matkul',MataKuliahController.getByIdMatkul)

module.exports= route