// import { useAuthStore } from '@/stores';
import axios from 'axios';
import { useAuthStore } from '../stores/auth.store';

export const fetchWrapper = {
    get: await request('GET'),
    post: await request('POST'),
    put: await request('PUT'),
    delete:await request('DELETE'),
    patch: await request('PATCH')
};
async function request(method) {
    return async (url, body) => {
        const requestOptions = {
            method,
            url,
            headers: authHeader(url)
        };
        if (!requestOptions.headers) {
        if (!requestOptions.headers) {
            requestOptions.headers = {}; 
        }

        if (body) {
            requestOptions.headers['Content-Type'] = 'application/json';
            requestOptions.data = body; // Gunakan `data` untuk body di Axios
        }

        try {
            // Kirim request menggunakan Axios
            const response = await axios(requestOptions);
            return handleAxiosResponse(response); // Pastikan fungsi ini ada untuk memproses respons
        } catch (error) {
            console.error('Request failed', error);
            throw error;
        }
    };
}


// helper functions
function authHeader(url) {
    // MUST BE PROVIDED TO EACH REQUEST TO THE SERVER
    // return auth header with jwt if user is logged in and request is to the api url
    const { user } = useAuthStore();
    const isLoggedIn = !!user?.token;
    const isApiUrl = url.startsWith(import.meta.env.VITE_API_URL);
    if (!user) {
        console.error("User is null, unable to retrieve token");
        return {};  // No authorization header if user is null
    }
}

function handleAxiosResponse(response) {
    const isJson = response.headers['content-type']?.includes('application/json');
    const data = isJson ? response.data : null;  // Axios response data is accessed via `response.data`

    // Check for error response
    if (response.status < 200 || response.status >= 300) {
        const { user, logout } = useAuthStore();
        if ([401, 403].includes(response.status) && user) {
            if(response.data.data.message.includes("JWT expired")){
                alert('Session Anda telah berakhir, silakan login kembali untuk melanjutkan.')
            }else if(response.data.data.message.includes("Access is denied")){
                alert("Permintaan Anda tidak dapat diproses karena Anda tidak punya hak untuk mengakses permintaan tersebut.")
            }
            logout();  // auto logout if 401 or 403 response
        }

        const error = (data && (data.data?.message || data.message)) || response.statusText;
        return Promise.reject(error);
    }

    return data;   
}

}
