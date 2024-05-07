<script setup>
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useRoute, useRouter } from "vue-router";
import { ref, onMounted } from "vue";
import axios from "axios";

import { useAlertStore } from "@/stores";

const alertStore = useAlertStore();
const route = useRoute();
const router = useRouter();
const kodeJadwal = route.params.kode_jadwal;
const baseUrl = "http://localhost:3000/jadwal";
const dataApi = ref({
  data: [],
  loading: false,
  error: null,
});

const fetchDataJadwal = async (url) => {
  try {
    const response = await axios.get(url);
    dataApi.value.data = response.data.jadwal.dataJadwal;
    console.log('Data by kode jadwal:', dataApi.value.data);
  } catch (error) {
    alertStore.error("Failed to fetch data");
    console.log(kodeJadwal);
  }
};

if (kodeJadwal) {
  onMounted(async () => {
    try {
      await fetchDataJadwal(`${baseUrl}/${kodeJadwal}`);
    } catch (error) {
      alertStore.error("Failed to fetch data");
    }
  });
}

const schema = Yup.object().shape({
  kode_ruang: Yup.string().required("Kode ruang is required"),
  kode_sesi: Yup.string().required("Kode sesi is required"),
  tanggal: Yup.date().required("Tanggal is required"),
});

const editJadwal = async (values) => {
  try {
    const data = {
      kode_ruang: values.kode_ruang,
      kode_sesi: values.kode_sesi,
      tanggal: values.tanggal,
    };
    console.log("Data yang disend request:", data);
    await axios.patch(`${baseUrl}/${kodeJadwal}`, data);
    alertStore.success("Jadwal updated");
    router.push("/jadwal");
  } catch (error) {
    console.error("Failed to update jadwal:", error.message);
    alertStore.error("Failed to update jadwal");
  }
};

async function onSubmit(values) {
  try {
    await editJadwal(values);
  } catch (error) {
    alertStore.error("Failed to update jadwal");
  }
}
</script>

<template>
  <h1>Edit jadwal</h1>
  <template v-if="!(dataApi.loading || dataApi.error)">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-slot="{ errors, isSubmitting }"
      v-for="data in dataApi.data" :key="data.kode_jadwal"
      :initial-values="data"
    >
      <div class="form-row">
        <div class="form-group col">
          <label>kode ruang</label>
          <Field
            name="kode_ruang"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kode_ruang }"
          />
          <div class="invalid-feedback">{{ errors.kode_ruang }}</div>
        </div>
        <div class="form-group col">
          <label>kode sesi</label>
          <Field
            name="kode_sesi"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kode_sesi }"
          />
          <div class="invalid-feedback">{{ errors.kode_sesi }}</div>
        </div>
        <div class="form-group col">
          <label>Tanggal</label>
          <Field
            name="tanggal"
            type="date"
            class="form-control"
            :class="{ 'is-invalid': errors.tanggal }"
          />
          <div class="invalid-feedback">{{ errors.tanggal }}</div>
        </div>
      </div>
      <div class="form-group">
        <button class="btn btn-primary" :disabled="isSubmitting">
          <span
            v-show="isSubmitting"
            class="spinner-border spinner-border-sm mr-1"
          ></span>
          Save
        </button>
        <router-link to="/jadwal" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="dataApi.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="dataApi.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading: {{ dataApi.error }}
      </div>
    </div>
  </template>
</template>
