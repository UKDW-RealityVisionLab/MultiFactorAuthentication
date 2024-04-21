import { Layout, List, Add, Edit } from '@/views/kelas';

export default {
    path: '/kelas',
    component: Layout,
    children: [
        { path: '', component: List },
        { path: 'add', component: Add },
        { path: ':kode_kelas', component: Edit }
    ]
};
