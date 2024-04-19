const moment = require('moment');

const logReq = (req, res, next) => {
    console.log('Terjadi request di ', req.path);
    const timestamp = moment().format('YYYY-MM-DD HH:mm:ss');
    console.log(`${timestamp} - ${req.method} ${req.originalUrl}`);

    // Set CORS headers
    res.setHeader('Access-Control-Allow-Origin', 'http://localhost:3001');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, DELETE');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    next();
};

module.exports = logReq;
