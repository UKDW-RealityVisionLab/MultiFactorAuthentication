<script setup>
import axios from 'axios';
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const kode_jadwal = route.params.kode_jadwal;
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
    const response = await axios.get(baseURL + "/" + kode_jadwal);
    console.log('API response:', response.data.presensi);
    const responseData = response.data.presensi;
    dataApi.value.qrCode = responseData.data;
    const startTime = new Date(); // Waktu mulai saat QR code digenerate
    dataApi.value.startTime = startTime.toLocaleTimeString();
    const endTime = new Date(startTime.getTime() + 300 * 1000); // Waktu kadaluarsa QR code 
    dataApi.value.endTime = endTime.toLocaleTimeString();
    updateLiveTime(); // Memanggil fungsi untuk menginisialisasi live time

    // Menjalankan interval untuk memperbarui live time setiap detik
    const intervalId = setInterval(() => {
      updateLiveTime();
      const currentTime = new Date();
      // Memeriksa apakah waktu sekarang telah mencapai end time
      if (currentTime >= endTime) {
        clearInterval(intervalId); // Menghentikan interval jika waktu mencapai end time
      }
    }, 1000);
  } catch (error) {
    console.error("Error fetching data:", error);
    dataApi.value.error = "Failed to fetch data";
  }
  onMounted(showQr);
};

const updateLiveTime = () => {
  dataApi.value.liveTime = new Date().toLocaleTimeString(); // Mengupdate nilai live time setiap detik
};

function goTokehadiran() {
  window.open(router.resolve('/daftarpresensi').href, '_blank');
}



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
