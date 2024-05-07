import { Qr, Layout } from '@/views/QR';

export default {
    path: '/presensi',
    component: Layout,
    children: [
        // { path: '', component: Qr },
        { path: ':kode_jadwal', component: Qr },
    ]
};
