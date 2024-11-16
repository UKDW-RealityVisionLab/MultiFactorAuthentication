<script setup>
import { ref, onMounted } from 'vue';
import { useApp } from '../../stores/app.store.js';
import { router } from "@/router"
import path from '../../router/kelas.routes';

const dataKelas = ref({
  data: [],
  loading: false,
  error: null,
});


const fetchDataKelas = async () => {
  dataKelas.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getData(path.path);
    dataKelas.value.data = response;
    console.log("Data yang didapat:", dataKelas.value.data); 
  } catch (error) {
    dataKelas.value.error = error.message;
  } finally {
    dataKelas.value.loading = false;
  }
};

const deleteKelas = async (kodeKelas) => {
  dataKelas.value.loading = true;
  try {
    const app = useApp();
    await app.deleteData(path.path, kodeKelas);
    await fetchDataSemester();  
  } catch (error) {
    console.error("Error deleting data kelas:", error); 
  } finally {
    dataKelas.value.loading = false;  
  }
};

onMounted(() => {
  fetchDataKelas();
});

const selectKelas= (id)=>{
  router.push(`/jadwal/jadwalPresensi/${id}`)
};
</script>

<template>
  <h1>Kelas</h1>
  <router-link to="/kelas/add" class="btn btn-sm btn-success mb-2">Add Kelas</router-link>
  <table class="table table-striped">
    <thead>
      <tr>
        <th style="width: 20%">Kode Kelas</th>
        <th style="width: 20%">Matakuliah</th>
        <th style="width: 20%">Group Kelas</th>
        <th style="width: 20%">Kode Semester</th>
        <th style="width: 20%">Dosen</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="dataKelas.loading">
        <tr>
          <td colspan="8" class="text-center">
            <span class="spinner-border spinner-border-lg align-center"></span>
          </td>
        </tr>
      </template>
      
      <template v-else-if="dataKelas.error">
        <tr>
          <td colspan="8">
            <div class="text-danger">Error loading kelas: {{ dataKelas.error }}</div>
          </td>
        </tr>
      </template>

      <template v-else>
        <tr v-for="item in dataKelas.data" :key="item.kode_kelas">
          <td>{{ item.kodeKelas }}</td>
          <td>{{ item.matakuliah }}</td>
          <td>{{ item.grup }}</td>
          <td>{{ item.semester }}</td>
          <td>{{ item.dosen }}</td>
  
          <td style="white-space: nowrap">
            <router-link :to="`kelas/${item.kodeKelas}`" class="btn btn-sm btn-primary mr-1">Edit</router-link>
            <!-- <router-link :to="`jadwal/${item.kode_kelas}`" class="btn btn-sm btn-success mr-1">Pilih</router-link> -->
            <button class="btn btn-sm btn-success btn-pilih-kelas" @click="selectKelas(item.kodeKelas)">Pilih</button> 
            <button class="btn btn-sm btn-danger btn-delete-kelas" @click="deleteKelas(item.kodeKelas)">Delete</button> 
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>