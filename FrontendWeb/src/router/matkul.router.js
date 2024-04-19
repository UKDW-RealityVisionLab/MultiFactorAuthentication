import { Layout, List, Add, Edit } from '@/views/matkul';

export default {
    path: '/matakuliah',
    component: Layout,
    children: [
        { path: '', component: List },
        { path: 'add', component: Add },
        { path: ':kode_matakuliah', component: Edit }
    ]
};
