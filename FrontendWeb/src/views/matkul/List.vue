<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const baseUrl = "http://localhost:3000/matakuliah";
const matkul = ref({
  data: [],
  loading: false,
  error: null,
});

const fetchDataMatkul = async () => {
  matkul.value.loading = true;
  try {
    const response = await axios.get(baseUrl);
    matkul.value.data = response.data;
  } catch (error) {
    matkul.value.error = error.message;
  } finally {
    matkul.value.loading = false;
  }
};

const deleteMatkul = async (kodeMatakuliah) => {
  try {
    await axios.delete(`${baseUrl}/${kodeMatakuliah}`);
    await fetchDataMatkul();
  } catch (error) {
    console.error("Error deleting Mata Kuliah:", error);
    alertStore.error("Failed to delete Mata Kuliah");
  }
};


onMounted(() => {
  fetchDataMatkul();
});

</script>

<template>
  <h1>Mata kuliah</h1>
  <router-link to="/mataKuliah/add" class="btn btn-sm btn-success mb-2">Add Matkul</router-link>
  <table class="table table-striped">
    <thead>
      <tr>
        <th style="width: 30%">Kode mata kuliah</th>
        <th style="width: 30%">Nama Mata Kuliah</th>
        <th style="width: 25%">SKS</th>
        <th style="width: 25%">Harga</th>
        <th style="width: 25%">Praktikum</th>
        <th style="width: 25%">Minimal SKS</th>
        <th style="width: 25%">Tanggal Input</th>
        <th style="width: 25%">Aksi</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="matkul.loading">
        <tr>
          <td colspan="8" class="text-center">
            <span class="spinner-border spinner-border-lg align-center"></span>
          </td>
        </tr>
      </template>
      
      <template v-else-if="matkul.error">
        <tr>
          <td colspan="8">
            <div class="text-danger">Error loading matkul: {{ matkul.error }}</div>
          </td>
        </tr>
      </template>

      <template v-else>
        <tr v-for="mat in matkul.data" :key="mat.kode_matakuliah">
          <td>{{ mat.kode_matakuliah }}</td>
          <td>{{ mat.nama_matakuliah }}</td>
          <td>{{ mat.sks }}</td>
          <td>{{ mat.harga }}</td>
          <td>{{ mat.is_praktikum }}</td>
          <td>{{ mat.minimal_sks }}</td>
          <td>{{ mat.tanggal_input }}</td>
          <td style="white-space: nowrap">
            <router-link :to="`/matakuliah/${mat.kode_matakuliah}`" class="btn btn-sm btn-primary mr-1">Edit</router-link>
            <button class="btn btn-sm btn-danger btn-delete-matkul" @click="deleteMatkul(mat.kode_matakuliah)">Delete</button> 
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>