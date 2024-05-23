<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { router } from "@/router"

const baseUrl = "http://localhost:3000/kelas";
const kelass = ref({
  data: [],
  // loading: false,
  error: null,
});


const fetchDataKelas = async () => {
  // kelass.value.loading = true;
  try {
    const response = await axios.get(baseUrl);
    kelass.value.data = response.data.kelas.dataKelas;
  } catch (error) {
    kelass.value.error = error.message;
  } finally {
    // kelass.value.loading = false;
  }
};

const deleteKelas = async (kodeKelass) => {
  try {
    await axios.delete(`${baseUrl}/${kodeKelass}`);
    await fetchDataKelas();
  } catch (error) {
    console.error("Error deleting kelas:", error);
  }
};

onMounted(() => {
  fetchDataKelas();
});

const selectKelas= (id)=>{
  router.push(`/jadwal/jadwalPresensi/${id}`)
}

</script>

<template>
  <h1>Kelas</h1>
  <router-link to="/kelas/add" class="btn btn-sm btn-success mb-2">Add Kelas</router-link>
  <table class="table table-striped">
    <thead>
      <tr>
        <th style="width: 20%">Kode Kelas</th>
        <th style="width: 20%">Kode Matakuliah</th>
        <th style="width: 20%">Group Kelas</th>
        <th style="width: 20%">Kode Semester</th>
        <th style="width: 20%">Kode Dosen</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="kelass.loading">
        <tr>
          <td colspan="8" class="text-center">
            <span class="spinner-border spinner-border-lg align-center"></span>
          </td>
        </tr>
      </template>
      
      <template v-else-if="kelass.error">
        <tr>
          <td colspan="8">
            <div class="text-danger">Error loading kelas: {{ kelass.error }}</div>
          </td>
        </tr>
      </template>

      <template v-else>
        <tr v-for="item in kelass.data" :key="item.kode_kelas">
          <td>{{ item.kode_kelas }}</td>
          <td>{{ item.kode_matakuliah }}</td>
          <td>{{ item.group_kelas }}</td>
          <td>{{ item.kode_semester }}</td>
          <td>{{ item.nidn }}</td>
  
          <td style="white-space: nowrap">
            <router-link :to="`kelas/${item.kode_kelas}`" class="btn btn-sm btn-primary mr-1">Edit</router-link>
            <!-- <router-link :to="`jadwal/${item.kode_kelas}`" class="btn btn-sm btn-success mr-1">Pilih</router-link> -->
            <button class="btn btn-sm btn-success btn-pilih-kelas" @click="selectKelas(item.kode_kelas)">Pilih</button> 
            <button class="btn btn-sm btn-danger btn-delete-kelas" @click="deleteKelas(item.kode_kelas)">Delete</button> 
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>