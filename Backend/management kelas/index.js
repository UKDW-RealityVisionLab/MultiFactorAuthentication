const express= require('express');
const app = express();

const port= 8080;
const kelasRoute= require('./routes/kelas.js');

app.use('/kelas',kelasRoute);


app.listen(port,()=>{
    console.log('server sudah berjalan di port: '+ port)
})