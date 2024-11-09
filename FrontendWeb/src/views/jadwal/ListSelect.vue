<script setup>
import { ref, onMounted } from 'vue';
import { useApp } from '../../stores/app.store.js';
import { useRouter, useRoute } from "vue-router"; 
const route = useRoute()
const router = useRouter();
import path from '../../router/jadwal.router';


const dataApi = ref({
  data: [],
  loading: false,
  error: null,
});

let idJadwal= route.params.kode_kelas

const fetchDataJadwal = async () => {
  dataApi.value.loading = true;
 
  try {
    const app = useApp();
    const response = await app.getDataByIdJadwal(path.path,idJadwal);
    console.log(`path= ${path.path},id jadwal= ${idJadwal}=`)
    dataApi.value.data = response;
    console.log("Data yang didapat:", dataApi.value.data); 
  } catch (error) {
    dataApi.value.error = error.message;
  } finally {
    dataApi.value.loading = false;
  }
};

const deleteJadwal = async (kode_kelas) => {
  dataApi.value.loading = true;
  try {
    const app = useApp();
    await app.deleteData(path.path, kode_kelas);
    await fetchDataJadwal();  
  } catch (error) {
    console.error("Error deleting data jadwal:", error); 
  } finally {
    dataApi.value.loading = false;  
  }
};

const presensi= (kode_jadwal)=>{
  router.push(`/presensi/${kode_jadwal}`)
}

onMounted(() => {
  fetchDataJadwal();
});
</script>

<template>
  <h1>Jadwal</h1>
  <router-link to="/jadwal/add" class="btn btn-sm btn-success mb-2">Add jadwal</router-link>
  <table class="table table-striped">
    <thead>
      <tr>
        <th style="width: 30%">Kode jadwal</th>
        <th style="width: 30%">kode ruang</th>
        <th style="width: 25%">Kode sesi</th>
        <th style="width: 25%">tanggal</th>
        <th style="width: 25%">kode_kelas</th>
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
        <tr v-for="jadwalApi in dataApi.data" :key="jadwalApi.kode_jadwal">
          <td>{{ jadwalApi.jadwal}}</td>
          <td>{{ jadwalApi.ruang }}</td>
          <td>{{ jadwalApi.sesi }}</td>
          <td>{{ jadwalApi.tanggal }}</td>
          <td>{{ jadwalApi.kodeKelas }}</td>

          
          <td style="white-space: nowrap">
            <router-link :to="`/jadwal/${jadwalApi.jadwal}`" class="btn btn-sm btn-primary mr-1">Edit</router-link>
            <button class="btn btn-sm btn-success btn-presensi" @click="presensi(jadwalApi.jadwal)">QR</button> 
            <button class="btn btn-sm btn-danger btn-delete" @click="deleteJadwal(jadwalApi.jadwal)">Delete</button> 
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>