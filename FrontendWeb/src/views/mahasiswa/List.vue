<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const baseUrl = "http://localhost:3000/users";
const dataMahasiswa = ref({
  data: [],
  // loading: false,
  error: null,
});

const fetchDataMahasiswa = async () => {
  // kelass.value.loading = true;
  try {
    const response = await axios.get(baseUrl);
    dataMahasiswa.value.data = response.data.user.dataUser;
  } catch (error) {
    dataMahasiswa.value.error = error.message;
  } finally {
    // kelass.value.loading = false;
  }
};

const deleteMahasiswa = async (kodedataMahasiswa) => {
  try {
    await axios.delete(`${baseUrl}/${kodedataMahasiswa}`);
    await fetchDataMahasiswa();
  } catch (error) {
    console.error("Error deleting mahasiswa:", error);
  }
};

onMounted(() => {
  fetchDataMahasiswa();
});

</script>

<template>
  <h1>Mahasiswa</h1>
  <router-link to="/mahasiswa/add" class="btn btn-sm btn-success mb-2">Add Mahasiswa</router-link>
  <table class="table table-striped">
    <thead>
      <tr>
        <th style="width: 20%">NIM</th>
        <th style="width: 20%">Kode Prodi</th>
        <th style="width: 20%">Tahun Angkatan</th>
        <th style="width: 20%">Nama</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="dataMahasiswa.loading">
        <tr>
          <td colspan="8" class="text-center">
            <span class="spinner-border spinner-border-lg align-center"></span>
          </td>
        </tr>
      </template>
      
      <template v-else-if="dataMahasiswa.error">
        <tr>
          <td colspan="8">
            <div class="text-danger">Error loading mahasiswa: {{ dataMahasiswa.error }}</div>
          </td>
        </tr>
      </template>

      <template v-else>
        <tr v-for="item in dataMahasiswa.data" :key="item.nim">
          <td>{{ item.nim }}</td>
          <td>{{ item.kode_prodi }}</td>
          <td>{{ item.tahun_angkatan }}</td>
          <td>{{ item.nama }}</td>
  
          <td style="white-space: nowrap">
            <router-link :to="`mahasiswa/${item.nim}`" class="btn btn-sm btn-primary mr-1">Edit</router-link>
            <button class="btn btn-sm btn-danger btn-delete-mahasiswa" @click="deleteMahasiswa(item.nim)">Delete</button> 
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>