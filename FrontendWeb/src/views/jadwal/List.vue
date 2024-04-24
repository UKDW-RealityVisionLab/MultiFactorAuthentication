<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const baseUrl = "http://localhost:3000/jadwal";
const dataApi = ref({
  data: [],
  loading: false,
  error: null,
});

const fetchDataJadwal = async () => {
  dataApi.value.loading = true;
  try {
    const response = await axios.get(baseUrl);
    dataApi.value.data = response.data.jadwal.dataJadwal;
  } catch (error) {
    dataApi.value.error = error.message;
  } finally {
    dataApi.value.loading = false;
  }
};

const deleteJadwal = async (kodeJadwal) => {
  try {
    await axios.delete(`${baseUrl}/${kodeJadwal}`);
    await fetchDataJadwal();
  } catch (error) {
    console.error("Error deleting :", error);
    alertStore.error("Failed to delete ");
  }
};


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
          <td>{{ jadwalApi.kode_jadwal}}</td>
          <td>{{ jadwalApi.kode_ruang }}</td>
          <td>{{ jadwalApi.kode_sesi }}</td>
          <td>{{ jadwalApi.tanggal }}</td>
          
          <td style="white-space: nowrap">
            <router-link :to="`/jadwal/${jadwalApi.kode_jadwal}`" class="btn btn-sm btn-primary mr-1">Edit</router-link>
            <button class="btn btn-sm btn-danger btn-delete" @click="deleteJadwal(jadwalApi.kode_jadwal)">Delete</button> 
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>