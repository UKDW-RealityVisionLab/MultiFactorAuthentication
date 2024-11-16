<script setup>
import { ref, onMounted } from 'vue';
import { useApp } from '../../stores/app.store.js';

import path from '../../router/ruang.router';
const ruang = ref({
  data: [],
  loading: false,
  error: null,
});

const fetchDataRuang = async () => {
  ruang.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getData(path.path);
    ruang.value.data = response;
    console.log("Data yang didapat:", ruang.value.data); 
  } catch (error) {
    ruang.value.error = error.message;
  } finally {
    ruang.value.loading = false;
  }
};

const deleteRuang = async (kodeRuang) => {
  ruang.value.loading = true;
  try {
    const app = useApp();
    await app.deleteData(path.path, kodeRuang);
    await fetchDataRuang();  
  } catch (error) {
    console.error("Error deleting data semester:", error); 
  } finally {
    ruang.value.loading = false;  
  }
};


onMounted(() => {
  fetchDataRuang();
});

</script>

<template>
  <h1>Ruang</h1>
  <router-link to="/ruang/add" class="btn btn-sm btn-success mb-2">Add ruang</router-link>
  <table class="table table-striped">
    <thead>
      <tr> 
        <th style="width: 30%">Kode ruang</th>
        <th style="width: 30%">Nama ruang</th>
        <th style="width: 25%">Latitude</th>
        <th style="width: 25%">Longitude</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="ruang.loading">
        <tr>
          <td colspan="8" class="text-center">
            <span class="spinner-border spinner-border-lg align-center"></span>
          </td>
        </tr>
      </template>
      
      <template v-else-if="ruang.error">
        <tr>
          <td colspan="8">
            <div class="text-danger">Error loading matkul: {{ ruang.error }}</div>
          </td>
        </tr>
      </template>

      <template v-else>
        <tr v-for="ruang in ruang.data" :key="ruang.kodeRuang">
          <td>{{ ruang.kodeRuang }}</td>
          <td>{{ ruang.nama }}</td>
          <td>{{ ruang.latitude }}</td>
          <td>{{ ruang.longitude }}</td>
          
          <td style="white-space: nowrap">
            <router-link :to="`/ruang/${ruang.kodeRuang}`" class="btn btn-sm btn-primary mr-1">Edit</router-link>
            <button class="btn btn-sm btn-danger btn-delete-ruang" @click="deleteRuang(ruang.kodeRuang)">Delete</button> 
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>