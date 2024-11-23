import { defineStore } from 'pinia';

import { fetchWrapper } from '@/helpers';
import { useAuthStore } from '@/stores';

const baseUrl = `${import.meta.env.VITE_API_URL}/users`;

export const useUsersStore = defineStore({
    id: 'users',
    state: () => ({
        users: {},
        user: {}
    }),
    actions: {
        async register(user) {
            /*
            * Kode di bawah ini di-comment karena /register di backend belom dibuat,
            * jadi kode di bawah ini hanya mencoba mencoba mengirim data user ke API register.
            * kalo misal udah dibuat, di-uncomment aja
            */
            // await fetchWrapper.post(`${baseUrl}/register`, user);
            console.log("hello world");
            let storedArray = JSON.parse(localStorage.getItem('mfa-feapps')) || []; // Default to empty array if null

            // Step 2: Append a new value to the array
            storedArray.push(user);  // You can replace 'newItem' with any value you want to add

            // Step 3: Store the updated array back to localStorage
            localStorage.setItem('mfa-feapps', JSON.stringify(storedArray));
        },
        async getAll() {
            this.users = { loading: true };
            // try {
            //     this.users = await fetchWrapper.get(baseUrl);    
            // } catch (error) {
            //     this.users = { error };
            // }
            const usersLocal = JSON.parse(localStorage.getItem("mfa-feapps"));
            console.log("get all return user:",usersLocal);
            this.users = usersLocal;
        },
        async getById(id) {
            this.user = { loading: true };
            try {
                this.user = await fetchWrapper.get(`${baseUrl}/${id}`);
            } catch (error) {
                this.user = { error };
            }
        },
        async update(id, params) {
            await fetchWrapper.put(`${baseUrl}/${id}`, params);

            // update stored user if the logged in user updated their own record
            const authStore = useAuthStore();
            if (id === authStore.user.id) {
                // update local storage
                const user = { ...authStore.user, ...params };
                localStorage.setItem('user', JSON.stringify(user));

                // update auth user in pinia state
                authStore.user = user;
            }
        },
        async delete(id) {
            // add isDeleting prop to user being deleted
            this.users.find(x => x.id === id).isDeleting = true;

            await fetchWrapper.delete(`${baseUrl}/${id}`);

            // remove user from list after deleted
            this.users = this.users.filter(x => x.id !== id);

            // auto logout if the logged in user deleted their own record
            const authStore = useAuthStore();
            if (id === authStore.user.id) {
                authStore.logout();
            }
        }
    }
});
