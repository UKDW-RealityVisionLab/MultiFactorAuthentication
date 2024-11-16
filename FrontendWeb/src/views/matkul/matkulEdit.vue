<script setup>
import { useApp } from '../../stores/app.store.js';
import path from '../../router/matkul.router';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter, useRoute } from "vue-router"; 
import { ref, onMounted } from 'vue';
import { fetchWrapper } from '@/helpers';
const alertStore = useAlertStore();
const router = useRouter();
const route = useRoute();
const kodeMatakuliah = route.params.kode_matakuliah;
let praktik=  ref(true);
const dataMatkul = ref({
  dataId: [],
  loading: false,
  error: null,
});

const schema = Yup.object().shape({
  nama_matakuliah: Yup.string(),
  sks: Yup.number()
    // .required("SKS is required")
    .min(0, "SKS must be at least 0")
    .max(9, "SKS must be at most 9"),
  harga: Yup.number(),
  is_praktikum: Yup.string(),
  minimal_sks: Yup.number()
    // .required("Minimal SKS is required")
    .min(0, "Minimal SKS must be at least 1"),
  tanggal_input: Yup.date(),
});

const fetchDataMatakuliah = async () => {
  dataMatkul.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getDataById(path.path, kodeMatakuliah);
    dataMatkul.value.dataId = response; 
    console.log("Data yang didapat:", dataMatkul.value.dataId); 
    praktik.value = dataMatkul.value.dataId[0].is_praktikum;
    if(praktik.value=="Yes"){
      praktik.value=true
    }

    console.log("praktik:", praktik)
    
  } catch (error) {
    dataMatkul.value.error = error.message;
  } finally {
    dataMatkul.value.loading = false;
  }
  // return praktik
};


onMounted(() => {
  if (kodeMatakuliah) {
    fetchDataMatakuliah().then(() => {
      console.log("Final value of praktik:", praktik.value);
    });
  }
});


async function onSubmit(values) {
    const alertStore = useAlertStore();
    const app = useApp();
    try {
        dataMatkul.value.loading = true;
        const data = {
          nama_matakuliah: values.nama_matakuliah,
          sks: values.sks,
          harga: values.harga,
          is_praktikum: praktik.value ? "Yes" : "No",
          minimal_sks: values.minimal_sks,
          tanggal_input: values.tanggal_input,
        };
        dataMatkul.value.loading = true;
        console.log(path);
        await app.editData(path.path, kodeMatakuliah, data);
        await router.push(path.path);
        alertStore.success("Mata kuliah update successfully");
    } catch (error) {
        alertStore.error(error.message || "Failed to update");
    } finally {
        dataMatkul.value.loading = false;
    }
};

</script>

<template>
  <h1>Edit mata kuliah</h1>
  <template v-if="!(dataMatkul?.loading || dataMatkul?.error)">
    <Form @submit="onSubmit" :validation-schema="schema" v-slot="{ errors, isSubmitting }"  v-for="mat in dataMatkul.dataId" :key="mat.kode_matakuliah" :initial-values="mat">
      <div class="form-row">
        <div class="form-group col">
          <label>Nama Mata Kuliah</label>
          <Field name="nama_matakuliah" type="text" class="form-control"
            :class="{ 'is-invalid': errors.namaMataKuliah }" />
          <div class="invalid-feedback">{{ errors.namaMataKuliah }}</div>
        </div>
        <div class="form-group col">
          <label>SKS</label>
          <Field name="sks" type="number" class="form-control" :class="{ 'is-invalid': errors.sks }" />
          <div class="invalid-feedback">{{ errors.sks }}</div>
        </div>
        <div class="form-group col">
          <label>Harga</label>
          <Field name="harga" type="number" class="form-control" :class="{ 'is-invalid': errors.harga }" />
          <div class="invalid-feedback">{{ errors.harga }}</div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group col">
          <label> Praktikum </label>
          <input name="is_praktikum"  type="checkbox" class="form-check-input" v-model="praktik"   />
          <div class="invalid-feedback">{{ errors.is_praktikum }}</div>
        </div>
        <div class="form-group col">
          <label>Minimal SKS</label>
          <Field name="minimal_sks" type="number" class="form-control" :class="{ 'is-invalid': errors.minimalSks }" />
          <div class="invalid-feedback">{{ errors.minimalSks }}</div>
        </div>
        <div class="form-group col">
          <label>Tanggal Input</label>
          <Field name="tanggal_input" type="date" class="form-control" :class="{ 'is-invalid': errors.tanggalInput }" />
          <div class="invalid-feedback">{{ errors.tanggalInput }}</div>
        </div>
      </div>
      <div class="form-group">
        <button class="btn btn-primary" :disabled="isSubmitting">
          <span v-show="isSubmitting" class="spinner-border spinner-border-sm mr-1"></span>
          Save
        </button>
        <router-link to="/matakuliah" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="dataMatkul?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="dataMatkul?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Mata Kuliah: {{ dataMatkul.error }}
      </div>
    </div>
  </template>
</template> 