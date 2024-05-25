<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const baseUrl = "http://localhost:3000/sesi";
const sesi = ref({
  data: [],
  loading: false,
  error: null,
});

const fetchDataSesi = async () => {
  sesi.value.loading = true;
  try {
    const response = await axios.get(baseUrl);
    sesi.value.data = response.data;
    console.log("Data yang didapat:", sesi.value.data); 
  } catch (error) {
    sesi.value.error = error.message;
  } finally {
    sesi.value.loading = false;
  }
};


onMounted(() => {
  fetchDataSesi();
});

</script>

<template>
  <h1>Sesi kelas</h1>

  <table class="table table-striped">
    <thead>
      <tr>
        <th style="width: 30%">Kode sesi</th>
        <th style="width: 30%">Start sesi</th>
        <th style="width: 30%">End sesi</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="sesi.loading">
        <tr>
          <td colspan="3" class="text-center"> 
            <span class="spinner-border spinner-border-lg align-center"></span>
          </td>
        </tr>
      </template>
      
      <template v-else-if="sesi.error">
        <tr>
          <td colspan="3"> 
            <div class="text-danger">Error loading matkul: {{ sesi.error }}</div>
          </td>
        </tr>
      </template>

      <template v-else>
        <tr v-for="item in sesi.data" :key="item.kode_sesi"> 
          <td>{{ item.kode_sesi }}</td>
          <td>{{ item.sesi_start }}</td>
          <td>{{ item.sesi_end }}</td>
        </tr>
      </template>
    </tbody>
  </table>
</template>
