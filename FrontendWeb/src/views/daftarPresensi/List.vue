<script setup>
import { ref, onMounted } from 'vue';
import { useApp } from '../../stores/app.store.js';
import { router } from "@/router"
import { useRouter, useRoute } from "vue-router"; 
import path from '../../router/daftarPresensi.router';
const route = useRoute();
const kodeJadwal= route.params.kode_jadwal

const dataApi = ref({
  data: [],
  loading: false,
  error: null,
});
const fetchData = async () => {
  dataApi.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getDataById(path.path, kodeJadwal);
    dataApi.value.data = response;
    console.log("Data yang didapat:", dataApi.value.data); 
  } catch (error) {
    dataApi.value.error = error.message;
  } finally {
    dataApi.value.loading = false;
  }
};

onMounted(() => {
  fetchData();
});

</script>

<template>
  <h1>Daftar presensi</h1>
  <table class="table table-striped">
    <thead>
      <tr>
        <th style="width: 30%">NIM</th>
        <th style="width: 30%">NAMA</th>
        <th style="width: 30%">STATUS</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="dataApi.loading">
        <tr>
          <td colspan="8" class="text-center">
            <span class="spinner-border spinner-border-lg align-center"></span>
          </td>
        </tr>
      </template>
      
      <template v-else-if="dataApi.error">
        <tr>
          <td colspan="8">
            <div class="text-danger">Error loading: {{ dataApi.error }}</div>
          </td>
        </tr>
      </template>

      <template v-else>
        <tr v-for="api in dataApi.data" :key="api.nim">
          <td>{{ api.nim_mahasiswa}}</td>
          <td>{{ api.nama }}</td> 
          <td>{{ api.hadir }}</td>
        </tr>
      </template>
    </tbody>
  </table>
</template>