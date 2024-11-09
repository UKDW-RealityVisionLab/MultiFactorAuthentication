<script setup>
import { ref } from 'vue';
import { useApp } from '../../stores/app.store.js';
import path from '../../router/kelas.routes';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter } from 'vue-router';

const router = useRouter();

const dataKelas = ref({
  data: [],
  loading: false,
  error: null,
});

async function onSubmit(values) {
  const alertStore = useAlertStore();
  const app = useApp();
  dataKelas.value.loading = true;

  try {
    const newKelas = {
      kode_kelas: values.kode_kelas,
      kode_matakuliah: values.kode_matakuliah,
      group_kelas: values.group_kelas,
      kode_semester: values.kode_semester,
      kode_dosen: values.kode_dosen
    };

    await app.addData(newKelas, path.path);
    alertStore.success("Kelas added successfully");
    await router.push(path.path);

  } catch (error) {
    dataKelas.value.error = "Failed to add kelas. Please try again.";
    alertStore.error("Failed to add kelas");
  } finally {
    dataKelas.value.loading = false;
  }
};
</script>

<template>
  <h1>Add Kelas</h1>
  <template v-if="!(dataKelas?.loading || dataKelas?.error)">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-slot="{ errors, isSubmitting }"
    >
      <div class="form-row">
        <div class="form-group col">
          <label>Kode Kelas</label>
          <Field
            name="kode_kelas"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kode_kelas }"
          />
          <div class="invalid-feedback">{{ errors.kode_kelas }}</div>
        </div>
        <div class="form-group col">
          <label>Kode Matakuliah</label>
          <Field
            name="kode_matakuliah"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kode_matakuliah }"
          />
          <div class="invalid-feedback">{{ errors.kode_matakuliah }}</div>
        </div>
        <div class="form-group col">
          <label>Group Kelas</label>
          <Field
            name="group_kelas"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.group_kelas }"
          />
          <div class="invalid-feedback">{{ errors.group_kelas }}</div>
        </div>
        <div class="form-group col">
          <label>Kode Semester</label>
          <Field
            name="kode_semester"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kode_semester }"
          />
          <div class="invalid-feedback">{{ errors.kode_semester }}</div>
        </div>
        <div class="form-group col">
          <label>Kode Dosen</label>
          <Field
            name="kode_dosen"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kode_dosen }"
          />
          <div class="invalid-feedback">{{ errors.kode_dosen }}</div>
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
        <router-link to="/kelas" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="dataKelas?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="dataKelas?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Kelas: {{ dataKelas.error }}
      </div>
    </div>
  </template>
</template>
