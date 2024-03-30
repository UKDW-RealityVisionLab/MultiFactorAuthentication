const getKelas = (req, res) => {
  const dummy = {
    id: 1,
    user: "aa",
    pw: "jahahskdaklfd",
  };
  res.json({
    message: "anda berhasil get data kelas",
    data: dummy,
  });
};

const addKelas = (req, res) => {
  res.json({
    message: "anda berhasil add data kelas",
    req: req.body,
  });
  console.log(req.body);
};

const editKelas = (req, res) => {
  const { id } = req.params;
  console.log("id yang diedit:", id);
  res.json({
    message: "anda berhasil edit data kelas",
    data: req.body,
  });
};

const deleteKelas = (req, res) => {
  const { id } = req.params;
  console.log("id yang dihapus:", id);
  res.json({
    message: "anda berhasil hapus data kelas dengan id" + " "+id,
    data: {
      id: 1,
      user: "aa",
      pw: "jahahskdaklfd",
    },
  });
};
module.exports = { getKelas, addKelas, editKelas, deleteKelas };
