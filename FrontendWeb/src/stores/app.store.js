import { defineStore } from 'pinia';
import { fetchWrapper } from '@/helpers';
import { router } from '@/router';
import { useAlertStore } from '@/stores';
import axios from 'axios';

const baseUrl = `${import.meta.env.VITE_API_URL}`;

export const useApp = defineStore({
    id: 'app',
    state: () => ({
        // initialize state from local storage to enable user to stay logged in
        user: JSON.parse(localStorage.getItem('user')),
        returnUrl: null
    }),
    actions: {        
        async getData(endpoint) {
            try {
                return await fetchWrapper.get(`${baseUrl}${endpoint}`);
            } catch (error) {
                const alertStore = useAlertStore();
                alertStore.error(error.message || 'Failed to get data');
            }
        },
        async getDataById(endpoint, id) {
            try {
                return await fetchWrapper.get(`${baseUrl}${endpoint}/${id}`); 
            } catch (error) {
                const alertStore = useAlertStore();
                alertStore.error(error.message || 'Failed to get data');
            }
        },
        async getDataByIdJadwal(endpoint, id) {
            try {
                return await fetchWrapper.post(`${baseUrl}${endpoint}/jadwalPresensi/${id}`); 
            } catch (error) {
                const alertStore = useAlertStore();
                alertStore.error(error.message || 'Failed to get data');
            }
        },
        async getDataByIdRuang(endpoint, id) {
            try {
                return await fetchWrapper.get(`${baseUrl}${endpoint}/getEdit/${id}`); 
            } catch (error) {
                const alertStore = useAlertStore();
                alertStore.error(error.message || 'Failed to get data');
            }
        },
        async addData(data, endpoint) {
            try {
                const response = await fetchWrapper.post(`${baseUrl}${endpoint}`, data);
                const alertStore = useAlertStore();
                alertStore.success("Success to add");
                return response;
            } catch (error) {
                const alertStore = useAlertStore();
                alertStore.error("Failed to add");
                throw error;
            }
        },
        async editData(endpoint, id, data) {
            try {
                const alertStore = useAlertStore();
                const response = await fetchWrapper.patch(`${baseUrl}${endpoint}/${id}`, data);
                console.log(`urlEdit=${baseUrl}${endpoint}/${id}`, data);
                alertStore.success("Success to update");
                return response;
            } catch (error) {
                const alertStore = useAlertStore();
                alertStore.error("Failed to update");
                throw error;
            }
        },
        
        async deleteData(endpoint, id) {
            try {
                console.log(`Success to delete ${id}`)
                return await fetchWrapper.delete(`${baseUrl}${endpoint}/${id}`);
            } catch (error) {
                const alertStore = useAlertStore();
                alertStore.error(error.message || 'Failed to delete data');
                throw error; 
            }
          },
    }
});
