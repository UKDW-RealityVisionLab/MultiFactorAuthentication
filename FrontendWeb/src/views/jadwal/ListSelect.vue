<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from "vue-router";

const baseUrl = "http://localhost:3000/jadwal";
const route = useRoute();
const router = useRouter();
const kode_kelas = route.params.kode_kelas;
const dataApi = ref({
  data: [],
  loading: false,
  error: null,
});

const fetchDataJadwal = async (url) => {
  try {
    const response = await axios.post(url);
    dataApi.value.data = response.data;
    console.log('Data by kode kelas:', dataApi.value.data);
  } catch (error) {
    console.error("Error fetching data:", error);
    dataApi.value.error = "Failed to fetch data";
  }
};

const deleteJadwal = async (kodeJadwal) => {
  try {
    await axios.delete(`${baseUrl}/${kodeJadwal}`);
    await fetchDataJadwal(`${baseUrl}/${kode_kelas}`);
  } catch (error) {
    console.error("Error deleting jadwal:", error);
    dataApi.value.error = "Failed to delete jadwal";
  }
};

const presensi= (kode_jadwal)=>{
  router.push(`/presensi/${kode_jadwal}`)
}

onMounted(async () => {
  try {
    await fetchDataJadwal(`${baseUrl}/jadwalPresensi/${kode_kelas}`);
  } catch (error) {
    console.error("Error fetching data:", error);
    dataApi.value.error = "Failed to fetch data";
  }
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