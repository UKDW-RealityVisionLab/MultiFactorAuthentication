import { Layout, List, Add, Edit } from '@/views/jadwal';

export default {
    path: '/jadwal',
    component: Layout,
    children: [
        { path: '', component: List },
        { path: 'add', component: Add },
        { path: ':kode_jadwal', component: Edit }
    ]
};
