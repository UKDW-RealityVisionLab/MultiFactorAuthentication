<script setup>
import { useAuthStore } from '@/stores';

const authStore = useAuthStore();
</script>

<template>
    <nav v-show="authStore.user" class="navbar navbar-expand navbar-dark bg-dark">
        <div class="navbar-nav">
            <router-link to="/" class="nav-item nav-link">Home</router-link>
            <router-link to="/users" class="nav-item nav-link">Users</router-link>
            <router-link to="/matakuliah" class="nav-item nav-link">Mata kuliah</router-link>
            <router-link to="/kelas" class="nav-item nav-link">Kelas</router-link>
            <router-link to="/sesi" class="nav-item nav-link">Sesi kelas</router-link>
            <router-link to="/ruang" class="nav-item nav-link">ruang </router-link>
            <router-link to="/jadwal" class="nav-item nav-link">jadwal </router-link>
            <router-link to="/mahasiswa" class="nav-item nav-link">Mahasiswa</router-link>
            <button @click="authStore.logout()" class="btn btn-link nav-item nav-link">Logout</button>
        </div>
    </nav>
</template>