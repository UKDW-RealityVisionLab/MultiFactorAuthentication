<script setup>
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useRoute } from "vue-router";
// import { storeToRefs } from 'pinia';
import { ref, onMounted } from "vue";
import axios from "axios";

import { useUsersStore, useAlertStore } from "@/stores";
import { router } from "@/router";

const usersStore = useUsersStore();
const alertStore = useAlertStore();
const route = useRoute();
const kode_matakuliah = route.params.kode_matakuliah;
const baseUrl = "http://localhost:3000/matakuliah";
const matkul = ref({
  dataId: [],
  loading: false,
  //   error: null,
});


const fetchMataKuliahById = async (url) => {
  try {
    const response = await axios.get(url)
    matkul.value.dataId = response.data;
  } catch (error) {
    alertStore.error("Failed to fetch mata kuliah data");
  }
};

let title = "Add Mata Kuliah";
// let mataKuliah = null;
if (kode_matakuliah) {
  // edit mode
  title = "Edit Mata Kuliah";
  try {
   fetchMataKuliahById(`${baseUrl}/${kode_matakuliah}`);
  } catch (error) {
    alertStore.error("Failed to fetch mata kuliah data");
  }
}

onMounted(() => {
  // fetchMataKuliahById;
});

const schema = Yup.object().shape({
  kodeMataKuliah: Yup.string().required("Kode Mata Kuliah is required"),
  namaMataKuliah: Yup.string().required("Nama Mata Kuliah is required"),
  sks: Yup.number()
    .required("SKS is required")
    .min(0, "SKS must be at least 0")
    .max(9, "SKS must be at most 9"),
  harga: Yup.number().required("Harga is required"),
  praktikum: Yup.boolean().value = false,
  minimalSks: Yup.number()
    .required("Minimal SKS is required")
    .min(0, "Minimal SKS must be at least 1"),
  tanggalInput: Yup.date().required("Tanggal Input is required"),
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

async function onSubmit(values) {
  try {
    let message;
    if (kode_matakuliah) {
      // edit mode
      try {
        // await usersStore.update(user.value.id, values);
        // message = "User updated";
      } catch (error) {
        alertStore.error("Failed to update");
      }
    } else {
      try {
        const newMatkul = {
          kode_matakuliah: values.kodeMataKuliah,
          nama_matakuliah: values.namaMataKuliah,
          sks: values.sks,
          harga: values.harga,
          is_praktikum: values.praktikum ? values.praktikum : true,
          minimal_sks: values.minimalSks,
          tanggal_input: values.tanggalInput,
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
  <template v-if="!(mataKuliah?.loading || mataKuliah?.error)">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      :initial-values="kode_matakuliah"
      v-slot="{ errors, isSubmitting }"
    >
      <div class="form-row">
        <div class="form-group col">
          <label>Kode Mata Kuliah</label>
          <Field
            name="kodeMataKuliah"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kodeMataKuliah }"
          />
          <div class="invalid-feedback">{{ errors.kodeMataKuliah }}</div>
        </div>
        <div class="form-group col">
          <label>Nama Mata Kuliah</label>
          <Field
            name="namaMataKuliah"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.namaMataKuliah }"
          />
          <div class="invalid-feedback">{{ errors.namaMataKuliah }}</div>
        </div>
        <div class="form-group col">
          <label>SKS</label>
          <Field
            name="sks"
            type="number"
            class="form-control"
            :class="{ 'is-invalid': errors.sks }"
          />
          <div class="invalid-feedback">{{ errors.sks }}</div>
        </div>
        <div class="form-group col">
          <label>Harga</label>
          <Field
            name="harga"
            type="number"
            class="form-control"
            :class="{ 'is-invalid': errors.harga }"
          />
          <div class="invalid-feedback">{{ errors.harga }}</div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group col">
      <label> Praktikum </label>
      <Field name="praktikum" type="checkbox" class="form-check-input" v-model="praktikum" />
    </div>
        <div class="form-group col">
          <label>Minimal SKS</label>
          <Field
            name="minimalSks"
            type="number"
            class="form-control"
            :class="{ 'is-invalid': errors.minimalSks }"
          />
          <div class="invalid-feedback">{{ errors.minimalSks }}</div>
        </div>
        <div class="form-group col">
          <label>Tanggal Input</label>
          <Field
            name="tanggalInput"
            type="date"
            class="form-control"
            :class="{ 'is-invalid': errors.tanggalInput }"
          />
          <div class="invalid-feedback">{{ errors.tanggalInput }}</div>
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
        <router-link to="/mata-kuliah" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="mataKuliah?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="mataKuliah?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Mata Kuliah: {{ mataKuliah.error }}
      </div>
    </div>
  </template>
</template>
