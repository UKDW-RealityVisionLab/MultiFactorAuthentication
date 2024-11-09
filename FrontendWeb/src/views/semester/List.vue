<script setup>
import { ref, onMounted } from 'vue';
import { useApp } from '../../stores/app.store.js';

import path from '../../router/semester.routes';
const dataSemester = ref({
  data: [],
  loading: false,
  error: null,
});

const fetchDataSemester = async () => {
  dataSemester.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getData(path.path);
    dataSemester.value.data = response;
    console.log("Data yang didapat:", dataSemester.value.data); 
  } catch (error) {
    dataSemester.value.error = error.message;
  } finally {
    dataSemester.value.loading = false;
  }
};

const deleteSemester = async (kode_semester) => {
  dataSemester.value.loading = true;
  try {
    const app = useApp();
    await app.deleteData(path.path, kode_semester);
    await fetchDataSemester();  
  } catch (error) {
    console.error("Error deleting data semester:", error); 
  } finally {
    dataSemester.value.loading = false;  
  }
};


onMounted(() => {
  fetchDataSemester();
});
</script>

<template>
  <h1>Semester</h1>
  <router-link to="/semester/add" class="btn btn-sm btn-success mb-2">Add Semester</router-link>
  <table class="table table-striped">
    <thead>
      <tr>
        <th style="width: 20%">Kode Semester</th>
        <th style="width: 20%">Tahun Ajaran</th>
        <th style="width: 20%">Tanggal Mulai</th>
        <th style="width: 20%">Tanggal Selesai</th>
        <th style="width: 20%">Actions</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="dataSemester.loading">
        <tr>
          <td colspan="5" class="text-center">
            <span class="spinner-border spinner-border-lg align-center"></span>
          </td>
        </tr>
      </template>
      
      <template v-else-if="dataSemester.error">
        <tr>
          <td colspan="5">
            <div class="text-danger">Error loading data semester: {{ dataSemester.error }}</div>
          </td>
        </tr>
      </template>

      <template v-else>
        <tr v-for="item in dataSemester.data" :key="item.kode_semester">
          <td>{{ item.kode_semester }}</td>
          <td>{{ item.tahun_ajaran }}</td>
          <td>{{ item.tanggal_mulai }}</td>
          <td>{{ item.tanggal_selesai }}</td>
  
          <td style="white-space: nowrap">
            <router-link :to="`semester/${item.kode_semester}`" class="btn btn-sm btn-primary mr-1">Edit</router-link>
            <button class="btn btn-sm btn-danger btn-delete-data-semester" @click="deleteSemester(item.kode_semester)">Delete</button> 
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>
