import { Layout, List, AddEdit } from '@/views/presensi';

export default {
    path: '/presensi',
    component: Layout,
    children: [
        { path: '', component: List },
        { path: 'add', component: AddEdit },
        { path: 'edit/:id_presensi', component: AddEdit }
    ]
};
