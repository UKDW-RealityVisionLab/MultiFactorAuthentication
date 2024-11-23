import { defineStore } from 'pinia';

import { fetchWrapper } from '@/helpers';
import { router } from '@/router';
import { useAlertStore } from '@/stores';

const baseUrl = `${import.meta.env.VITE_API_URL}/users`;

export const useAuthStore = defineStore({
    id: 'auth',
    state: () => ({
        // initialize state from local storage to enable user to stay logged in
        user: JSON.parse(localStorage.getItem('user')),
        returnUrl: null
    }),
    actions: {
        async login(username, password) {
            try {
                /*
                * Kode di bawah ini di-comment karena /register di backend belom dibuat,
                * jadi kode di bawah ini hanya mencoba mencoba mengirim data user ke API register.
                * kalo misal udah dibuat, di-uncomment aja
                */
                // const user = await fetchWrapper.post(`${baseUrl}/authenticate`, { username, password });    
                const users = JSON.parse(localStorage.getItem("mfa-feapps"));
                console.log(users);
                const [user] = users.filter(user => user.username === username && user.password === password);
                // update pinia state
                this.user = user;

                // store user details and jwt in local storage to keep user logged in between page refreshes
                localStorage.setItem('user', JSON.stringify(user));

                // redirect to previous url or default to home page
                router.push(this.returnUrl || '/');
            } catch (error) {
                const alertStore = useAlertStore();
                alertStore.error(error);                
            }
        },
        logout() {
            this.user = null;
            localStorage.removeItem('user');
            router.push('/account/login');
        }
    }
});

