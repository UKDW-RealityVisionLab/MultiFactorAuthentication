<script setup>
import { ref, onMounted } from 'vue';

import path from '../../router/qr.router';
import { useRouter, useRoute } from "vue-router"; 
import { useApp } from '../../stores/app.store.js';
const route = useRoute()
const router = useRouter();

const kode_jadwal = route.params.kode_jadwal;

const dataApi = ref({
  qrCode: null,
  startTime: null,
  endTime: null,
  liveTime: null,
  loading: false,
  error: null,
});

const isGenerating = ref(false); // Variable to track whether QR is being generated

const showQr = async () => {
  try {
    isGenerating.value = true; // Set the flag to indicate QR generation is in progress
    const fun= useApp()
    const resp = await fun.getDataById(path.path, kode_jadwal);
    const response = { data: resp}
    console.log('API response:', response);
    const responseData = response.data
    dataApi.value.qrCode = responseData;
    const startTime = new Date(); 
    dataApi.value.startTime = startTime.toLocaleTimeString();
    const endTime = new Date(startTime.getTime() + 5 * 60000); 
    dataApi.value.endTime = endTime.toLocaleTimeString();
    updateLiveTime(); 

    const intervalId = setInterval(() => {
      updateLiveTime();
      const currentTime = new Date();
      if (currentTime >= endTime) {
        clearInterval(intervalId); // Stop the interval when the time reaches end time
        isGenerating.value = false; // Set the flag to indicate QR generation is complete
      }
    }, 1000);
  } catch (error) {
    console.error("Error fetching data:", error);
    dataApi.value.error = "Failed to fetch data";
  }
};

const updateLiveTime = () => {
  dataApi.value.liveTime = new Date().toLocaleTimeString(); // Update live time value every second
};

function goTokehadiran() {
  window.open(router.resolve(`/daftarpresensi/${kode_jadwal}`).href, '_blank');
}
</script>

<template>
  <h1>PRESENSI</h1>
  <button class="btn btn-sm btn-success btn-presensi" @click="showQr" :disabled="isGenerating">Generate QR</button>
  <div v-if="dataApi.qrCode">
    <div>
      <img :src="dataApi.qrCode" alt="QR Code" />
    </div>
    <div>
      <p>Start Time: {{ dataApi.startTime }}</p>
      <p>End Time: {{ dataApi.endTime }}</p>
      <p>Live Time: {{ dataApi.liveTime }}</p> 
      <p v-if="dataApi.liveTime === dataApi.endTime">Time is over</p> 
      <button class="btn btn-sm btn-warning btn-daftarpresensi" @click="goTokehadiran()">cek kehadiran</button> 
    </div>
  </div>
  <div v-else>
    <p v-if="dataApi.loading">Loading...</p>
    <p v-if="dataApi.error">Failed to fetch data: {{ dataApi.error }}</p>
  </div>
</template>
