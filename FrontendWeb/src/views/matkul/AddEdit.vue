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
const kode_matakuliah = route.params.kode_matakuliah;
const baseUrl = "http://localhost:3000/matakuliah";
const matkul = ref({
  dataId: [],
  loading: false,
  error: null,
});

const fetchMataKuliahById = async (url) => {
  try {
    const response = await axios.get(url)
    matkul.value.dataId = response.data.matakuliah.dataMataKuliah;
    console.log('Data by kode matkul:', matkul.value.dataId)

  } catch (error) {
    alertStore.error("Failed to fetch mata kuliah data");
  }
};

let title = "Add Mata Kuliah";
if (kode_matakuliah) {
  title = "Edit Mata Kuliah";
  onMounted(async () => {
    try {
      await fetchMataKuliahById(`${baseUrl}/${kode_matakuliah}`);
    } catch (error) {
      alertStore.error("Failed to fetch mata kuliah data");
    }
  });
}


const schema = Yup.object().shape({
  // kodeMataKuliah: Yup.string().required("Kode Mata Kuliah is required"),
  nama_matakuliah: Yup.string(),
  sks: Yup.number()
    // .required("SKS is required")
    .min(0, "SKS must be at least 0")
    .max(9, "SKS must be at most 9"),
  harga: Yup.number(),
  is_praktikum: Yup.boolean(),
  minimal_sks: Yup.number()
    // .required("Minimal SKS is required")
    .min(0, "Minimal SKS must be at least 1"),
  tanggal_input: Yup.date(),
});

const addMatkul = async (data) => {
  matkul.value.loading = true;
  try {
    const response = await axios.post(baseUrl, data);
    alertStore.success(response.data.message);
  } catch (error) {
    matkul.value.error = error.message;
  } finally {
    matkul.value.loading = false;
  }
};

const editMataKuliah = async (values) => {
  let message;
  matkul.value.loading = true;
  try {
    const data = {
          nama_matakuliah: values.nama_matakuliah,
          sks: values.sks,
          harga: values.harga,
          is_praktikum: values.is_praktikum || false, // Set default value if undefined
          minimal_sks: values.minimal_sks,
          tanggal_input: values.tanggal_input,
        };
    console.log("Data yang disend request:", data); // Add this line for debugging
    const response = await axios.patch(`${baseUrl}/${kode_matakuliah}`, data);
    alertStore.success(response.data.message);
    message = "Mata Kuliah updated kode kuliah="+kode_matakuliah;
  } catch (error) {
    if (error.response && error.response.status === 500) {
      console.error("Internal Server Error:", error.message);
    } else {
      console.error("Failed to update Mata Kuliah:", error.message);
    }
    alertStore.error("Failed to update Mata Kuliah");
    matkul.value.error = error.message;
  } finally {
    matkul.value.loading = false;
  }
};

async function onSubmit(values) {
  try {
    let message;
    if (kode_matakuliah) {
      try {
        // Ensure is_praktikum is set to false if it's undefined
        values.is_praktikum = values.is_praktikum || false;
        await editMataKuliah(values);
        console.log(values);
        await router.push("/matakuliah");
      } catch (error) {
        alertStore.error("Failed to update");
      }
    } else {
      try {
        const newMatkul = {
          nama_matakuliah: values.nama_matakuliah,
          sks: values.sks,
          harga: values.harga,
          is_praktikum: values.is_praktikum || false, // Set default value if undefined
          minimal_sks: values.minimal_sks,
          tanggal_input: values.tanggal_input,
        };

        await addMatkul(newMatkul);
        message = "Mata Kuliah added";
        await router.push("/matakuliah");
      } catch (error) {
        alertStore.error("Failed to add Mata Kuliah");
      }
    }
    alertStore.success(message);
  } catch (error) {
    alertStore.error(error);
  }
}

</script>

<template>
  <h1>{{ title }}</h1>
  <template v-if="!(matkul?.loading || matkul?.error)">
    <Form @submit="onSubmit" :validation-schema="schema" v-slot="{ errors, isSubmitting }"  v-for="mat in matkul.dataId" :initial-values="mat">
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
          <Field name="is_praktikum" type="checkbox" class="form-check-input" />
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
        <router-link to="/mata-kuliah" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="matkul?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="matkul?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Mata Kuliah: {{ matkul.error }}
      </div>
    </div>
  </template>
</template>