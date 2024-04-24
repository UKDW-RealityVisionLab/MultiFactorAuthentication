import { Layout, List, Add, Edit } from '@/views/mahasiswa';

export default {
    path: '/mahasiswa',
    component: Layout,
    children: [
        { path: '', component: List },
        { path: 'add', component: Add },
        { path: ':nim', component: Edit }
    ]
};
