<script setup>
import axios from 'axios';
import { ref, onMounted } from 'vue';

const baseURL = 'http://localhost:3000/presensi'; 

const dataApi = ref({
  qrCode: null,
  startTime: null,
  endTime: null,
  liveTime: null,
  loading: false,
  error: null,
});

const showQr = async () => {
  try {
    const response = await axios.get(baseURL);
    console.log('API response:', response.data.presensi);
    const responseData = response.data.presensi;
    dataApi.value.qrCode = responseData.data;
    const startTime = new Date(); // Waktu mulai saat QR code digenerate
    dataApi.value.startTime = startTime.toLocaleTimeString();
    const endTime = new Date(startTime.getTime() + 5 * 60 * 1000); // Waktu kadaluarsa QR code (5 menit setelah startTime)
    dataApi.value.endTime = endTime.toLocaleTimeString();
    updateLiveTime(); // Memanggil fungsi untuk menginisialisasi live time
  } catch (error) {
    console.error("Error fetching data:", error);
    dataApi.value.error = "Failed to fetch data";
  }
};


const updateLiveTime = () => {
  dataApi.value.liveTime = new Date().toLocaleTimeString(); // Mengupdate nilai live time setiap detik
};

// Memanggil showQr saat komponen dimounted
onMounted(showQr);

// Memperbarui live time setiap detik
setInterval(updateLiveTime, 1000);
</script>

<template>
  <h1>PRESENSI</h1>
  <button class="btn btn-sm btn-success btn-presensi" @click="showQr">Generate QR</button>
  <div v-if="dataApi.qrCode">
    <div>
      <img :src="dataApi.qrCode" alt="QR Code" />
    </div>
    <div>
      <p>Start Time: {{ dataApi.startTime }}</p>
      <p>End Time: {{ dataApi.endTime }}</p>
      <p>Live Time: {{ dataApi.liveTime }}</p> <!-- Menampilkan waktu saat ini secara langsung -->
    </div>
  </div>
  <div v-else>
    <p v-if="dataApi.loading">Loading...</p>
    <p v-if="dataApi.error">Failed to fetch data: {{ dataApi.error }}</p>
  </div>
</template>
