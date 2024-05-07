<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const kode_jadwal = route.params.kode_jadwal;

const baseUrl = "http://localhost:3000/daftarpresensi";
const dataApi = ref({
  data: [],
  loading: false,
  error: null,
});
// const kode_jadwal= route.params.kode_jadwal
const fetchData = async () => {
  dataApi.value.loading = true;
  try {
    const response = await axios.get(baseUrl+"/"+kode_jadwal);
    dataApi.value.data = response.data.kelas.dataPresensiKelas;
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