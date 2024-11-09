<script setup>
import { ref } from 'vue';
import { useApp } from '../../stores/app.store.js';
import path from '../../router/jadwal.router';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter } from 'vue-router';

const router = useRouter();

const dataApi = ref({
  data: [],
  loading: false,
  error: null,
});

const schema = Yup.object().shape({
  kode_jadwal: Yup.string().required("kode jadwal is required"),
  kode_kelas: Yup.string().required("kode kelas is required"),
  kode_ruang: Yup.string().required("kode ruang is required"),
  kode_sesi: Yup.string().required("kode sesi is required"),
  tanggal: Yup.date()
});

async function onSubmit(values) {
  const alertStore = useAlertStore();
  const app = useApp();
  dataApi.value.loading = true;

  try {
    const newJadwal = {
      kode_jadwal: values.kode_jadwal,
      kode_kelas: values.kode_kelas,
      kode_ruang: values.kode_ruang,
      kode_sesi: values.kode_sesi, 
      tanggal: values.tanggal, 
    };

    await app.addData(newJadwal, path.path);
    alertStore.success("Jadwal added successfully");
    await router.push(path.path);

  } catch (error) {
    dataApi.value.error = "Failed to add jadwal. Please try again.";
    alertStore.error("Failed to add jadwal");
  } finally {
    dataApi.value.loading = false;
  }
};
</script>

<template>
  <h1>Add jadwal</h1>
  <template v-if="!(dataApi.loading || dataApi.error)">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-slot="{ errors, isSubmitting }"
    >
      <div class="form-row">
        <div class="form-group col">
          <label>kode jadwal</label>
          <Field
            name="kode_jadwal"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kode_jadwal }"
          />
          <div class="invalid-feedback">{{ errors.kode_jadwal }}</div>
        </div>

        <div class="form-group col">
          <label>kode kelas</label>
          <Field
            name="kode_kelas"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kode_kelas }"
          />
          <div class="invalid-feedback">{{ errors.kode_kelas }}</div>
        </div>

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
        Error loading : {{ dataApi.error }}
      </div>
    </div>
  </template>
</template>
