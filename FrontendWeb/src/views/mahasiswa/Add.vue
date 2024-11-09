<script setup>
import { ref } from 'vue';
import { useApp } from '../../stores/app.store.js';
import path from '../../router/mahasiswa.router';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter } from 'vue-router';

const router = useRouter();
const dataMahasiswa = ref({
  data: [],
  loading: false,
  error: null,
});


async function onSubmit(values) {
  const alertStore = useAlertStore();
  const app = useApp();
  dataMahasiswa.value.loading = true;

  try {
    const newMahasiswa = {
      nim: values.nim,
      kode_prodi: values.kode_prodi,
      tahun_angkatan: values.tahun_angkatan,
      nama: values.nama,
    };

    await app.addData(newMahasiswa, path.path);
    alertStore.success("mahasiswa added successfully");
    await router.push(path.path);

  } catch (error) {
    dataMahasiswa.value.error = "Failed to add mahasiswa. Please try again.";
    alertStore.error("Failed to add mahasiswa");
  } finally {
    dataMahasiswa.value.loading = false;
  }
};

</script>


<template>
  <h1>Add Mahasiswa</h1>
  <template v-if="!(dataMahasiswaa?.loading || dataMahasiswa?.error)">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-slot="{ errors, isSubmitting }"
    >
      <div class="form-row">
        <div class="form-group col">
          <label>NIM</label>
          <Field
            name="nim"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.nim }"
          />
          <div class="invalid-feedback">{{ errors.nim }}</div>
        </div>
        <div class="form-group col">
          <label>Kode Prodi</label>
          <Field
            name="kode_prodi"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kode_prodi }"
          />
          <div class="invalid-feedback">{{ errors.kode_prodi }}</div>
        </div>
        <div class="form-group col">
          <label>Tahun Angkatan</label>
          <Field
            name="tahun_angkatan"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.tahun_angkatan }"
          />
          <div class="invalid-feedback">{{ errors.tahun_angkatan }}</div>
        </div>
        <div class="form-group col">
          <label>Nama</label>
          <Field
            name="nama"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.nama }"
          />
          <div class="invalid-feedback">{{ errors.nama }}</div>
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
        <router-link to="/mahasiswa" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="dataMahasiswa?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="dataMahasiswa?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Kelas: {{ dataMahasiswa.error }}
      </div>
    </div>
  </template>
</template>
