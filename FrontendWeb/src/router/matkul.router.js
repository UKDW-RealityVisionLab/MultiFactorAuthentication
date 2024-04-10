import { Layout, List } from '@/views/matkul';

export default {
    path: '/matakuliah',
    component: Layout,
    children: [
        { path: '', component: List },
        // { path: 'add', component: AddEdit },
        // { path: 'edit/:id', component: AddEdit }
    ]
};
