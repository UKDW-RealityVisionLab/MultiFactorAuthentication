const moment = require('moment');

const logReq = (req,res,next) =>{
    console.log('Terjadi request di ',req.path);
    const timestamp = moment().format('YYYY-MM-DD HH:mm:ss');
    console.log(`${timestamp} - ${req.method} ${req.originalUrl}`);
    next();
}
module.exports=logReq