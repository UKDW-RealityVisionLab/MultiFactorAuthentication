const express = require('express');
const route = express.Router();

const controller= require('../controller/sesi');

route.get('/', controller.getSesi)
module.exports= route