import { Layout, List } from '@/views/sesi';

export default {
    path: '/sesi',
    component: Layout,
    children: [
        { path: '', component: List },
    
    ]
};
