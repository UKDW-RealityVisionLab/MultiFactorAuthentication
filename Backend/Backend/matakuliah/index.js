const express = require('express');
const app = express();
const port = 3000;

app.use(express.json());

const mataKuliahController = require('./controller/mataKuliahController');

app.get('/mataKuliah', mataKuliahController.getAllMataKuliah);
app.post('/mataKuliah', mataKuliahController.addMataKuliah);
app.put('/mataKuliah/:kode_matakuliah', mataKuliahController.updateMataKuliah);
app.delete('/mataKuliah/:kode_matakuliah', mataKuliahController.deleteMataKuliah);

app.listen(port, () => {
  console.log(`http://localhost:${port}`);
});
