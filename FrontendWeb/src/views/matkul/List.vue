<script setup>
import { ref, onMounted } from 'vue';
import { useApp } from '../../stores/app.store.js';

import path from '../../router/matkul.router';

const dataMatkul = ref({
  data: [],
  loading: false,
  error: null,
});

const fetchDataMatkul = async () => {
  dataMatkul.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getData(path.path);
    dataMatkul.value.data = response;
    console.log("Data yang didapat:", dataMatkul.value.data); 
  } catch (error) {
    dataMatkul.value.error = error.message;
  } finally {
    dataMatkul.value.loading = false;
  }
};

const deleteMatkul = async (kodeMatakuliah) => {
  dataMatkul.value.loading = true;
  try {
    const app = useApp();
    await app.deleteData(path.path, kodeMatakuliah);
    await fetchDataMatkul();  
  } catch (error) {
    console.error("Error deleting data mata kuliah:", error); 
  } finally {
    dataMatkul.value.loading = false;  
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
        <th style="width: 30%">Kode Mata Kuliah</th>
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
      <template v-if="dataMatkul.loading">
        <tr>
          <td colspan="8" class="text-center">
            <span class="spinner-border spinner-border-lg align-center"></span>
          </td>
        </tr>
      </template>
      
      <template v-else-if="dataMatkul.error">
        <tr>
          <td colspan="8">
            <div class="text-danger">Error loading matkul: {{ dataMatkul.error }}</div>
          </td>
        </tr>
      </template>

      <template v-else>
        <tr v-for="mat in dataMatkul.data" :key="mat.kode_matakuliah">
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