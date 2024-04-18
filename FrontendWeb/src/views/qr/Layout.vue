<template>
  <div class="container">
    <h1>QR Code</h1>
    <div class="form-group">
      <label for="inputText">Enter text or URL:</label>
      <input type="text" id="inputText" v-model="inputText" class="form-control" placeholder="Enter text or URL">
    </div>
    <button @click="generateQRCode" class="btn btn-primary">Generate QR Code</button>
    <div>&nbsp;</div>
    <div v-if="qrCode">

      <img :src="qrCode" alt="QR Code">
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      inputText: '',
      qrCode: ''
    };
  },
  methods: {
    generateQRCode() {
      // Generate QR Code using inputText
      // For simplicity, you can use a QR Code API like qrjs.io
      // Replace 'YOUR_QR_CODE_API_KEY' with your actual API key
      fetch(`https://api.qrserver.com/v1/create-qr-code/?data=${encodeURIComponent(this.inputText)}&size=200x200`)
        .then(response => response.blob())
        .then(blob => {
          this.qrCode = URL.createObjectURL(blob);
        })
        .catch(error => {
          console.error('Error generating QR Code:', error);
        });
    }
  }
};
</script>

<style scoped>
.container {
  max-width: 600px;
  margin: auto;
  text-align: center;
}
.form-group {
  margin-bottom: 20px;
}
.btn-primary {
  margin-top: 10px;
}
</style>
