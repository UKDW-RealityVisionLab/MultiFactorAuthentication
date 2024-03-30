const express= require('express');
const app = express();

const port= 8080;
const kelasRoute= require('./routes/kelas.js');
const logReq= require('./middleware/log.js')

// cuma log aja
app.use(logReq);
// ini untuk permission json body
app.use(express.json())

app.use('/kelas',kelasRoute);


app.listen(port,()=>{
    console.log('server sudah berjalan di port: '+ port)
})