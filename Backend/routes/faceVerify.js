const express = require('express');
const route = express.Router();

const controller = require('../controller/faceVerify')

route.post('/faceVerify', controller.presensIfvalid)


module.exports = route