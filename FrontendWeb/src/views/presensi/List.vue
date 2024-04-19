<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const baseUrl = "http://localhost:3000/presensi";
const presensis = ref({
  data: [],
  
  error: null,
});

const fetchDataPresensi = async () => {

  try {
    const response = await axios.get(baseUrl);
    presensis.value.data = response.data.kelas.dataKelas;
  } catch (error) {
    presensis.value.error = error.message;
  } finally {
    // presensis.value.loading = false;
  }
};

const deleteKelas = async (id_presensi) => {
  try {
    await axios.delete(`${baseUrl}/${id_presensi}`);
    await fetchDataPresensi();
  } catch (error) {
    console.error("Error deleting kelas:", error);
  }
};

onMounted(() => {
  fetchDataPresensi();
});

</script>

<template>
  <h1>Kelas</h1>
  <!-- <router-link to="/kelas/add" class="btn btn-sm btn-success mb-2">Add Kelas</router-link> -->
  <table class="table table-striped">
    <thead>
      <tr>
        <th style="width: 20%">Id Presensi</th>
        <th style="width: 20%">Jadwal</th>
        <th style="width: 20%">NIM</th>
        <th style="width: 20%">Hadir</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="presensis.loading">
        <tr>
          <td colspan="8" class="text-center">
            <span class="spinner-border spinner-border-lg align-center"></span>
          </td>
        </tr>
      </template>
      
      <template v-else-if="presensis.error">
        <tr>
          <td colspan="8">
            <div class="text-danger">Error loading kelas: {{ presensis.error }}</div>
          </td>
        </tr>
      </template>

      <template v-else>
        <tr v-for="item in presensis.data" :key="item.id_presensi">
          <td>{{ item.id_presensi }}</td>
          <td>{{ item.jadwal }}</td>
          <td>{{ item.nim_mahasiswa }}</td>
          <td>{{ item.hadir }}</td>
         
        </tr>
      </template>
    </tbody>
  </table>
</template>