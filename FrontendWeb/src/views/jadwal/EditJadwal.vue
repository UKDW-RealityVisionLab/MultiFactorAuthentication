<script setup>
import { useApp } from '../../stores/app.store.js';
import path from '../../router/jadwal.router';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter, useRoute } from "vue-router"; 
import { ref, onMounted } from 'vue';
import { fetchWrapper } from '@/helpers';
const alertStore = useAlertStore();
const router = useRouter();
const route = useRoute();
const kodeJadwal = route.params.kode_jadwal;

const dataApi = ref({
  dataId: [],
  loading: false,
  error: null,
});

const schema = Yup.object().shape({
  kode_ruang: Yup.string().required("Kode ruang is required"),
  kode_sesi: Yup.string().required("Kode sesi is required"),
  tanggal: Yup.date().required("Tanggal is required"),
});

const fetchDataJadwal = async () => {
  dataApi.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getDataById(path.path, kodeJadwal);
    dataApi.value.dataId = response; 
    console.log("Data yang didapat:", dataApi.value.dataId); 
  } catch (error) {
    dataApi.value.error = error.message;
  } finally {
    dataApi.value.loading = false;
  }
};

onMounted(() => {
  if (kodeJadwal) {
    fetchDataJadwal();
  }
});

async function onSubmit(values) {
    const alertStore = useAlertStore();
    const app = useApp();
    try {
        dataApi.value.loading = true;
        const data = {
          kode_ruang: values.kode_ruang,
          kode_sesi: values.kode_sesi,
          tanggal: values.tanggal,
        };
        dataApi.value.loading = true;
        console.log(path);
        await app.editData(path.path, kodeJadwal, data);
        await router.push(path.path);
        alertStore.success("Jadwal update successfully");
    } catch (error) {
        alertStore.error(error.message || "Failed to update");
    } finally {
        dataApi.value.loading = false;
    }
};
</script>

<template>
  <h1>Edit jadwal</h1>
  <template v-if="!(dataApi.loading || dataApi.error)">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-slot="{ errors, isSubmitting }"
      v-for="data in dataApi.dataId" :key="data.kodeJadwal"
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
