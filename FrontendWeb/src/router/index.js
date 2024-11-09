import { createRouter, createWebHistory } from 'vue-router';

import { useAuthStore, useAlertStore } from '@/stores';
import { Home } from '@/views';
import accountRoutes from './account.routes';
import usersRoutes from './users.routes';
import matkulRoutes from './matkul.router';
import kelasRoutes from './kelas.routes';
import sesiRoutes from './sesi.routes';
import ruang from './ruang.router';
import jadwal from './jadwal.router';
import mahasiswaRoutes from './mahasiswa.router';
import semesterRoutes from './semester.routes';
import qrRoutes from './qr.router';
import daftarPresensi from './daftarPresensi.router';

export const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    linkActiveClass: 'active',
    routes: [
        { path: '/', component: Home },
        { ...accountRoutes },
        { ...usersRoutes },
        {...matkulRoutes},
        { ...kelasRoutes },
        {...sesiRoutes},
        {...ruang},
        {...jadwal},
        {...mahasiswaRoutes},
        {...semesterRoutes},
        {...qrRoutes},
        {...daftarPresensi},
        // catch all redirect to home page
        { path: '/:pathMatch(.*)*', redirect: '/' }
    ]
});

router.beforeEach(async (to) => {
    // clear alert on route change
    const alertStore = useAlertStore();
    alertStore.clear();

    // redirect to login page if not logged in and trying to access a restricted page 
    const publicPages = ['/account/login', '/account/register'];
    const authRequired = !publicPages.includes(to.path);
    const authStore = useAuthStore();

    if (authRequired && !authStore.user) {
        authStore.returnUrl = to.fullPath;
        return '/account/login';
    }
});
