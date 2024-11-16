import { List, Layout } from '@/views/daftarPresensi';

export default {
    path: '/daftarpresensi',
    component: Layout,
    children: [
        // { path: '', component: List },
        { path: ':kode_jadwal', component: List },
    ]
};
