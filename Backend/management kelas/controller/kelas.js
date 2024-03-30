const getKelas = (req,res)=>{
    res.json({
        message:'anda berhasil get data kelas'
    })
}

const addKelas= (req,res)=>{
    res.json({
        message:'anda berhasil add data kelas'
    })
}

module.exports= {getKelas,addKelas}