<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const baseUrl = "http://localhost:3000/qr";
const qr = ref({
  data: [],
  loading: false,
  error: null,
});

const fetchDataQr = async () => {
  qr.value.loading = true;
  try {
    const response = await axios.get(baseUrl);
    qr.value.data = response.data.user.dataQR;
  } catch (error) {
    qr.value.error = error.message;
  } finally {
    qr.value.loading = false;
  }
};

onMounted(() => {
  fetchDataQr();
});

const generateQRCode = (qrValue) => {
  return `https://api.qrserver.com/v1/create-qr-code/?data=${encodeURIComponent(qrValue)}&size=200x200`;
};
</script>
<template>
  <div class="container">
    <h1>QR Code</h1>
    <div v-if="qr.loading">Loading...</div>
    <div v-else-if="qr.error">Error: {{ qr.error }}</div>
    
    <table class="table">
      <thead>
        <tr>
          <th>Time Remain</th>
          <th>Time Start</th>
          <th>Time End</th>
          <th>QR Code</th>
          <th>QR Value</th>
          <th>Kode QR</th>
          <th>Action</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="qrItem in qr.data" :key="qrItem.kode_qr">
          <td>{{ qrItem.time_remain }}</td>
          <td>{{ qrItem.time_start }}</td>
          <td>{{ qrItem.time_end }}</td>
          <td>
            <img :src="generateQRCode(qrItem.qr_value)" alt="QR Code" style="max-width: 100px;">
          </td>
          <td>
            <a :href="qrItem.qr_value" target="_blank">{{ qrItem.qr_value }}</a>
          </td>
          <td>{{ qrItem.kode_qr }}</td>
          <td>
            <!-- Add edit and delete buttons here if needed -->
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>



<style scoped>
.container {
  max-width: 800px;
  margin: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.table th, .table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.table th {
  background-color: #f2f2f2;
}
</style>
