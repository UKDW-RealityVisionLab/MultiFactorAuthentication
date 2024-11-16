import { Layout, List, Add, Edit, ListSelect} from '@/views/jadwal';

export default {
    path: '/jadwal',
    component: Layout,
    children: [
        { path: '', component: List },
        { path: 'add', component: Add },
        { path: ':kode_jadwal', component: Edit },
        { path:'jadwalPresensi/:kode_kelas', component:ListSelect}
    ]
};
