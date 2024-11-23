<script setup>
import { useAuthStore } from '@/stores';

const authStore = useAuthStore();
</script>

<template>
    <nav v-show="authStore.user" class="navbar navbar-expand navbar-dark bg-dark">
        <div class="navbar-nav">
            <router-link to="/" class="nav-item nav-link">Home</router-link>
            <router-link to="/users" class="nav-item nav-link">User</router-link>
            <router-link to="/matakuliah" class="nav-item nav-link">Mata Kuliah</router-link>
            <router-link to="/kelas" class="nav-item nav-link">Kelas</router-link>
            <router-link to="/sesi" class="nav-item nav-link">Sesi Kelas</router-link>
            <router-link to="/ruang" class="nav-item nav-link">Ruang </router-link>
            <router-link to="/jadwal" class="nav-item nav-link">Jadwal </router-link>
            <router-link to="/mahasiswa" class="nav-item nav-link">Mahasiswa</router-link>
            <router-link to="/semester" class="nav-item nav-link">Semester</router-link>
            <button @click="authStore.logout()" class="btn btn-link nav-item nav-link">Logout</button>
        </div>
    </nav>
</template>