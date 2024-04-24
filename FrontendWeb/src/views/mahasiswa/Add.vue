<script setup>
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useRoute } from "vue-router";
import { ref, onMounted } from "vue";
import axios from "axios";

import { useAlertStore } from "@/stores";
import { router } from "@/router";

const alertStore = useAlertStore();
const route = useRoute();
const baseUrl = "http://localhost:3000/users";


const mahasiswa = ref({
  loading: false,
  error: null,
});

const addMahasiswa = async (data) => {
  mahasiswa.value.loading = true;
  try {
    const response = await axios.post(baseUrl, data);
    alertStore.success(response.data.message);
  } catch (error) {
    mahasiswa.value.error = error.message;
  } finally {
    mahasiswa.value.loading = false;
  }
};

async function onSubmit(values) {
  try {
    const newMahasiswa = {
      nim: values.nim,
      kode_prodi: values.kode_prodi,
      tahun_angkatan: values.tahun_angkatan,
      nama: values.nama,
    };

    await addMahasiswa(newMahasiswa);
    alertStore.success("Mahasiswa added");
    await router.push("/mahasiswa");
  } catch (error) {
    alertStore.error("Failed to add Mahasiswa");
  }
}
</script>

<template>
  <h1>Add Mahasiswa</h1>
  <template v-if="!(mahasiswa?.loading || mahasiswa?.error)">
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
  <template v-if="mahasiswa?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="mahasiswa?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Kelas: {{ mahasiswa.error }}
      </div>
    </div>
  </template>
</template>
