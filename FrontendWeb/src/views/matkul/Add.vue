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
const baseUrl = "http://localhost:3000/matakuliah";
const schema = Yup.object().shape({
  nama_matakuliah: Yup.string().required("Nama Mata Kuliah is required"),
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
const matkul = ref({
  loading: false,
  error: null,
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
    const newMatkul = {
      nama_matakuliah: values.nama_matakuliah,
      sks: values.sks,
      harga: values.harga,
      is_praktikum: values.is_praktikum || false,
      minimal_sks: values.minimal_sks,
      tanggal_input: values.tanggal_input,
    };
    await addMatkul(newMatkul);
    alertStore.success("Mata Kuliah added");
    await router.push("/matakuliah");
  } catch (error) {
    alertStore.error("Failed to add Mata Kuliah");
  }
}
</script>

<template>
  <h1>Add mata kuliah</h1>
  <template v-if="!(matkul.loading || matkul.error)">
    <Form @submit="onSubmit" :validation-schema="schema" v-slot="{ errors, isSubmitting }">
      <div class="form-row">
        <div class="form-group col">
          <label>Nama Mata Kuliah</label>
          <Field name="nama_matakuliah" type="text" class="form-control"
            :class="{ 'is-invalid': errors.nama_matakuliah }" />
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
          <label> Praktikum </label>
          <Field name="is_praktikum" type="checkbox" class="form-check-input" />
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
        <router-link to="/mata-kuliah" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="matkul.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="matkul.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Mata Kuliah: {{ matkul.error }}
      </div>
    </div>
  </template>
</template>