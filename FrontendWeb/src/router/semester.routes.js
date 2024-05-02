import { Layout, List, Add, Edit } from '@/views/semester';

export default {
    path: '/semester',
    component: Layout,
    children: [
        { path: '', component: List },
        { path: 'add', component: Add },
        { path: ':kode_semester', component: Edit }
    ]
};
