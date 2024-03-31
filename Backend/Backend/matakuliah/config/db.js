const mysql = require("mysql");

const db = mysql.createConnection({
  host: "localhost",
  user: "root",
  password: "",
  database: "mfa_presensi",
  port : 3307
});

module.exports= db