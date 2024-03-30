const express = require('express');
const bodyParser = require('body-parser');
const morgan = require('morgan');
const app = express();
const port = 3000;

app.use(morgan('dev'));
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

let users = [
    { id: 1, nama: 'John Doe', nim: '1234567890', kodeProdi: 'TI', tahunAngkatan: 2020 },
    { id: 2, nama: 'Aryo Anditya', nim: '1234567891', kodeProdi: 'TI', tahunAngkatan: 2021 }
];

let nextId = 3; // Initialize the nextId variable

app.post('/user', (req, res) => {
    const { nama, nim, kodeProdi, tahunAngkatan } = req.body;
    const newUser = { id: nextId++, nama, nim, kodeProdi, tahunAngkatan }; // Generate id and increment nextId
    users.push(newUser);
    res.status(200).send('User berhasil ditambahkan');
});

app.get('/user', (req, res) => {
    res.json(users);
});

app.put('/user/:id', (req, res) => {
    const id = parseInt(req.params.id);
    const updateUser = req.body;
    const index = users.findIndex(user => user.id === id);
    if (index === -1) {
        res.status(404).json({ message: 'User not found' });
    } else {
        users[index] = { ...users[index], ...updateUser };
        res.json(users[index]);
    }
});

app.delete('/user/:id', (req, res) => {
    const id = parseInt(req.params.id);
    const index = users.findIndex(user => user.id === id);
    if (index === -1) {
        res.status(404).json({ message: 'User not found' });
    } else {
        users.splice(index, 1);
        // Adjust IDs
        for (let i = index; i < users.length; i++) {
            users[i].id -= 1;
        }
        res.send('Data User berhasil dihapus');
    }
});

app.listen(port, () => {
    console.log(`http://localhost:${port}`);
});
