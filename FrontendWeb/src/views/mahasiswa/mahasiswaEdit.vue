<script setup>
import { useApp } from '../../stores/app.store.js';
import path from '../../router/mahasiswa.router';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter, useRoute } from "vue-router"; 
import { ref, onMounted } from 'vue';
import { fetchWrapper } from '@/helpers';
const alertStore = useAlertStore();
const router = useRouter();
const route = useRoute();
const kodeMahasiswa = route.params.nim;

const dataMahasiswa = ref({
    dataId: [],
    loading: false,
    error: null,
});


const fetchDataMahasiswa = async () => {
  dataMahasiswa.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getDataById(path.path, kodeMahasiswa);
    dataMahasiswa.value.dataId = response; 
    console.log("Data yang didapat:", dataMahasiswa.value.dataId); 
  } catch (error) {
    dataMahasiswa.value.error = error.message;
  } finally {
    dataMahasiswa.value.loading = false;
  }
};

onMounted(() => {
  if (kodeMahasiswa) {
    fetchDataMahasiswa();
  }
});


async function onSubmit(values) {
    const alertStore = useAlertStore();
    const app = useApp();
    try {
        dataMahasiswa.value.loading = true;
        const data = {
            kode_prodi: values.kode_prodi,
            tahun_angkatan: values.tahun_angkatan,
            nama: values.nama,
        };
        dataMahasiswa.value.loading = true;
        console.log(path);
        await app.editData(path.path, kodeMahasiswa, data);
        await router.push(path.path);
        alertStore.success("Mahasiswa update successfully");
    } catch (error) {
        alertStore.error(error.message || "Failed to update");
    } finally {
        dataMahasiswa.value.loading = false;
    }
};
</script>

<template>
  <h1>Edit Mahasiswa</h1>
  <template v-if="!dataMahasiswa.loading && !dataMahasiswa.error">
    <Form
        @submit="onSubmit"
        :validation-schema="schema"
        v-for="data in dataMahasiswa.dataId"
        :initial-values="data"
        v-slot="{ errors, isSubmitting }"
      >
      <div class="form-row" :key="data.nim">
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
  <template v-if="dataMahasiswa.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="dataMahasiswa.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading mahasiswa: {{ dataMahasiswa.error }}
      </div>
    </div>
  </template>
</template>
