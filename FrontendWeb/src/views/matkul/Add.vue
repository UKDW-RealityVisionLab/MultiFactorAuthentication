<script setup>
import { ref } from 'vue';
import { useApp } from '../../stores/app.store.js';
import path from '../../router/matkul.router';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter } from 'vue-router';

const router = useRouter();

const dataMatkul = ref({
  data: [],
  loading: false,
  error: null,
});

const schema = Yup.object().shape({
  kode_matakuliah: Yup.string().required("Kode mata kuliah is required"),
  nama_matakuliah: Yup.string().required("Nama mata kuliah is required"),
  sks: Yup.number()
    .required("SKS is required")
    .min(0, "SKS must be at least 0")
    .max(9, "SKS must be at most 9"),
  harga: Yup.number(),
  is_praktikum: Yup.boolean(),
  minimal_sks: Yup.number()
    .required("Minimal SKS is required")
    .min(0, "Minimal SKS must be at least 0"),
  tanggal_input: Yup.date(),
});

const checkboxDefaultValue = ref(true);

async function onSubmit(values) {
  const alertStore = useAlertStore();
  const app = useApp();
  dataMatkul.value.loading = true;

  try {
    const newMatkul = {
      kode_matakuliah: values.kode_matakuliah,
      nama_matakuliah: values.nama_matakuliah,
      sks: values.sks,
      harga: values.harga,
      is_praktikum: checkboxDefaultValue.value ,
      minimal_sks: values.minimal_sks,
      tanggal_input: values.tanggal_input,
    };

    await app.addData(newMatkul, path.path);
    alertStore.success("Mata kuliah added successfully");
    await router.push(path.path);

  } catch (error) {
    dataMatkul.value.error = "Failed to add mata kuliah. Please try again.";
    alertStore.error("Failed to add mata kuliah");
  } finally {
    dataMatkul.value.loading = false;
  }
}
</script>

<template>
  <h1>Add Mata Kuliah</h1>
  <template v-if="!(dataMatkul.loading || dataMatkul.error)">
    <Form @submit="onSubmit" :validation-schema="schema" v-slot="{ errors, isSubmitting }">
      <div class="form-row">
        <div class="form-group col">
          <label>Kode Mata Kuliah</label>
          <Field name="kode_matakuliah" type="text" class="form-control" :class="{ 'is-invalid': errors.kode_matakuliah }" />
          <div class="invalid-feedback">{{ errors.kode_matakuliah }}</div>
        </div>

        <div class="form-group col">
          <label>Nama Mata Kuliah</label>
          <Field name="nama_matakuliah" type="text" class="form-control" :class="{ 'is-invalid': errors.nama_matakuliah }" />
          <div class="invalid-feedback">{{ errors.nama_matakuliah }}</div>
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
          <label>Praktikum</label>
          <input type="checkbox" v-model="checkboxDefaultValue" class="form-check-input" true-value="Yes" false-value="No"  />
          <!-- <p>req: {{ JSON.stringify(values, undefined, 2) }}</p> -->
        </div>

        <div class="form-group col">
          <label>Minimal SKS</label>
          <Field name="minimal_sks" type="number" class="form-control" :class="{ 'is-invalid': errors.minimal_sks }" />
          <div class="invalid-feedback">{{ errors.minimal_sks }}</div>
        </div>

        <div class="form-group col">
          <label>Tanggal Input</label>
          <Field name="tanggal_input" type="date" class="form-control" :class="{ 'is-invalid': errors.tanggal_input }" />
          <div class="invalid-feedback">{{ errors.tanggal_input }}</div>
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

  <template v-if="dataMatkul.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>

  <template v-if="dataMatkul.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Mata Kuliah: {{ dataMatkul.error }}
      </div>
    </div>
  </template>
</template>
