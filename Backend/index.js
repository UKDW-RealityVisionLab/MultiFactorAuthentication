const express= require('express');
const app = express();

const port= 3000;
const presensiRoute= require('./routes/presensi.js');

const kelasRoute= require('./routes/kelas.js');

const userRouter= require('./routes/users')
const logReq= require('./middleware/log.js')

// cuma log aja
app.use(logReq);
// ini untuk permission json body
app.use(express.json())
app.use('/presensi',presensiRoute);
app.use('/kelas',kelasRoute);
app.use('/users',userRouter)


app.listen(port,()=>{
    console.log('server sudah berjalan di port: '+ port)
})