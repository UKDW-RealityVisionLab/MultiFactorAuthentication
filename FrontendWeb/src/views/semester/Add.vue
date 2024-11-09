<script setup>
import { ref } from 'vue';
import { useApp } from '../../stores/app.store.js';
import path from '../../router/semester.routes';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter } from 'vue-router';

const router = useRouter();

const dataSemester = ref({
  data: [],
  loading: false,
  error: null,
});

const schema = Yup.object().shape({
  kode_semester: Yup.number().required("Kode Semester is required"),
  tahun_ajaran: Yup.string().required("Tahun ajaran is required"),
  tanggal_mulai: Yup.date(),
  tanggal_selesai: Yup.date(),
});

async function onSubmit(values) {
  const alertStore = useAlertStore();
  const app = useApp();
  dataSemester.value.loading = true;

  try {
    const newSemester = {
      kode_semester: values.kode_semester,
      tahun_ajaran: values.tahun_ajaran,
      tanggal_mulai: values.tanggal_mulai,
      tanggal_selesai: values.tanggal_selesai,
    };

    await app.addData(newSemester, path.path);
    alertStore.success("Semester added successfully");
    await router.push(path.path);

  } catch (error) {
    dataSemester.value.error = "Failed to add semester. Please try again.";
    alertStore.error("Failed to add semester");
  } finally {
    dataSemester.value.loading = false;
  }
};
</script>

<template>
  <h1>Add Semester</h1>
  <template v-if="!dataSemester.loading && !dataSemester.error">
    <Form @submit="onSubmit" :validation-schema="schema" v-slot="{ errors, isSubmitting }">
      <div class="form-row">
        <div class="form-group col">
          <label>Kode Semester</label>
          <Field name="kode_semester" type="text" class="form-control" :class="{ 'is-invalid': errors.kode_semester }" />
          <div class="invalid-feedback">{{ errors.kode_semester }}</div>
        </div>
        <div class="form-group col">
          <label>Tahun Ajaran</label>
          <Field name="tahun_ajaran" type="text" class="form-control" :class="{ 'is-invalid': errors.tahun_ajaran }" />
          <div class="invalid-feedback">{{ errors.tahun_ajaran }}</div>
        </div>
        <div class="form-group col">
          <label>Tanggal Mulai</label>
          <Field name="tanggal_mulai" type="date" class="form-control" :class="{ 'is-invalid': errors.tanggal_mulai }" />
          <div class="invalid-feedback">{{ errors.tanggal_mulai }}</div>
        </div>
        <div class="form-group col">
          <label>Tanggal Selesai</label>
          <Field name="tanggal_selesai" type="date" class="form-control" :class="{ 'is-invalid': errors.tanggal_selesai }" />
          <div class="invalid-feedback">{{ errors.tanggal_selesai }}</div>
        </div>
      </div>
      <div class="form-group">
        <button class="btn btn-primary" :disabled="isSubmitting">
          <span v-show="isSubmitting" class="spinner-border spinner-border-sm mr-1"></span>
          Save
        </button>
        <router-link to="/semester" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>

  <template v-if="dataSemester.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>

  <template v-if="dataSemester.error">
    <div class="text-center m-5">
      <div class="text-danger">
        {{ dataSemester.error }}
      </div>
    </div>
  </template>
</template>
