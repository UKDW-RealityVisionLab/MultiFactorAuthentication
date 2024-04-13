import { Layout, List, AddEdit } from '@/views/matkul';

export default {
    path: '/matakuliah',
    component: Layout,
    children: [
        { path: '', component: List },
        { path: 'add', component: AddEdit },
        { path: ':kode_matakuliah', component: AddEdit }
    ]
};
