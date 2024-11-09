<script setup>
import { useApp } from '../../stores/app.store.js';
import path from '../../router/ruang.router';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter, useRoute } from "vue-router"; 
import { ref, onMounted } from 'vue';
import { fetchWrapper } from '@/helpers';
const alertStore = useAlertStore();
const router = useRouter();
const route = useRoute();

const Ruangkode = route.params.kode_ruang;

const dataRuang = ref({
  dataId: [],
  loading: false,
  error: null,
});

const schema = Yup.object().shape({
  nama: Yup.string().required("Nama is required"),
  latitude: Yup.number()
    .required("Latitude is required")
    .min(-90, "Latitude must be at least -90")
    .max(90, "Latitude must be at most 90"),
  longitude: Yup.number()
    .required("Longitude is required")
    .min(-180, "Longitude must be at least -180")
    .max(180, "Longitude must be at most 180"),
});

const fetchDataRuang = async () => {
  dataRuang.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getDataByIdRuang(path.path, Ruangkode);
    dataRuang.value.dataId = response; 
    console.log("Data yang didapat:", dataRuang.value.dataId); 
  } catch (error) {
    dataRuang.value.error = error.message;
  } finally {
    dataRuang.value.loading = false;
  }
};

onMounted(() => {
  if (Ruangkode) {
    fetchDataRuang();
  }
});

async function onSubmit(values) {
    const alertStore = useAlertStore();
    const app = useApp();
    try {
        dataRuang.value.loading = true;
        const data = {
            nama: values.nama,
            latitude: values.latitude, 
            longitude: values.longitude, 
        };
        dataRuang.value.loading = true;
        console.log(path);
        await app.editData(path.path, Ruangkode, data);
        await router.push(path.path);
        alertStore.success("Ruang update successfully");
    } catch (error) {
        alertStore.error(error.message || "Failed to update");
    } finally {
        dataRuang.value.loading = false;
    }
};
</script>

<template>
    <h1>Edit Ruang</h1>
    <template v-if="!dataRuang.loading && !dataRuang.error">
      <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-for="data in dataRuang.dataId"
      :initial-values="data"
      v-slot="{ errors, isSubmitting }"
      >
        <div class="form-row" :key="data.Ruangkode">
          <div class="form-group col">
            <label>Nama Ruang</label>
            <Field name="nama" type="text" class="form-control"
              :class="{ 'is-invalid': errors.nama }" />
            <div class="invalid-feedback">{{ errors.nama }}</div>
          </div>
          <div class="form-group col">
            <label>Latitude</label>
            <Field name="latitude" type="number" class="form-control" :class="{ 'is-invalid': errors.latitude }" />
            <div class="invalid-feedback">{{ errors.latitude }}</div>
          </div>
          <div class="form-group col">
            <label>Longitude</label>
            <Field name="longitude" type="number" class="form-control" :class="{ 'is-invalid': errors.longitude }" />
            <div class="invalid-feedback">{{ errors.longitude }}</div>
          </div>
        </div>
        <div class="form-group">
          <button class="btn btn-primary" :disabled="isSubmitting">
            <span v-show="isSubmitting" class="spinner-border spinner-border-sm mr-1"></span>
            Save
          </button>
          <router-link to="/ruang" class="btn btn-link">Cancel</router-link>
        </div>
      </Form>
    </template>
    <template v-if="dataRuang?.loading">
      <div class="text-center m-5">
        <span class="spinner-border spinner-border-lg align-center"></span>
      </div>
    </template>
    <template v-if="dataRuang?.error">
      <div class="text-center m-5">
        <div class="text-danger">
          Error loading ruang: {{ dataRuang.error }}
        </div>
      </div>
    </template>
  </template>