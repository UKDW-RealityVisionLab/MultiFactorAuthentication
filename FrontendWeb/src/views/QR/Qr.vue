<script setup>
import axios from 'axios';
const baseUrl = "http://localhost:3000/presensi";
import { ref, onMounted } from "vue";

const dataApi = ref({
  data: [],
  loading: false,
  error: null,
});

const generateQr = async (url) => {
  try {
    const response = await axios.get(url);
    dataApi.value.data = response.data.presensi.data;
    console.log('Data fetch:', dataApi.value.data);
  } catch (error) {
    console.error("Error fetching data:", error);
    dataApi.value.error = "Failed to fetch data";
  }
};

const showQr = async () => {
  try {
    await generateQr(`${baseUrl}`);
    // Setelah generateQr selesai, tampilkan QR code
    const qrData = dataApi.value.data[0]; // Misalnya mengambil data pertama dari array data
    const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${qrData}`;
    document.getElementById('qrCodeImage').src = qrCodeUrl; // Menyimpan URL QR code ke img src
  } catch (error) {
    console.error("Failed to fetch data:", error);
    alert("Failed to fetch data");
  }
}
</script>

<template>
    <h1>PRESENSI</h1>
    <button class="btn btn-sm btn-success btn-presensi" @click="showQr">Generate QR</button>
    <div>
        <img id="qrCodeImage" alt="QR Code" />
    </div>
</template>
