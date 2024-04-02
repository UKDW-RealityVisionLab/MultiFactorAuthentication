const logReq = (req,res,next) =>{
    console.log('Terjadi request di ',req.path);
    next();
}
module.exports=logReq