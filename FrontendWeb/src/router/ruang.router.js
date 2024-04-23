import { Layout, List, Add, Edit } from '@/views/ruang';

export default {
    path: '/ruang',
    component: Layout,
    children: [
        { path: '', component: List },
        { path: 'add', component: Add },
        { path: ':kode_ruang', component: Edit }
    ]
};
