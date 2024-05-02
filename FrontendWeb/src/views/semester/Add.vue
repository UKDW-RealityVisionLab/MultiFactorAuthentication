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
const baseUrl = "http://localhost:3000/semester";


const dataSemester = ref({
  loading: false,
  error: null,
});

const schema = Yup.object().shape({
  kode_semester: Yup.number()
    .required("Kode Semester is required"),
  tahun_ajaran: Yup.string().required("Tahun ajaran is required"),
  tanggal_mulai: Yup.date(),
  tanggal_selesai: Yup.date(),
});

const addSemester = async (data) => {
  dataSemester.value.loading = true;
  try {
    const response = await axios.post(baseUrl, data);
    alertStore.success(response.data.message);
  } catch (error) {
    dataSemester.value.error = error.message;
  } finally {
    dataSemester.value.loading = false;
  }
};

async function onSubmit(values) {
  try {
    const newSemester = {
      kode_semester: values.kode_semester,
      tahun_ajaran: values.tahun_ajaran,
      tanggal_mulai: values.tanggal_mulai,
      tanggal_selesai: values.tanggal_selesai,
    };

    await addSemester(newSemester);
    alertStore.success("Semester added");
    await router.push("/semester");
  } catch (error) {
    alertStore.error("Failed to add Semester");
  }
}
</script>

<template>
  <h1>Add Mahasiswa</h1>
  <template v-if="!(dataSemester?.loading || dataSemester?.error)">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-slot="{ errors, isSubmitting }"
    >
      <div class="form-row">
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
          <label>Tahun Ajaran</label>
          <Field
            name="tahun_ajaran"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.tahun_ajaran }"
          />
          <div class="invalid-feedback">{{ errors.tahun_ajaran }}</div>
        </div>
        <div class="form-group col">
          <label>Tanggal Mulai</label>
          <Field
            name="tanggal_mulai"
            type="date"
            class="form-control"
            :class="{ 'is-invalid': errors.tanggal_mulai }"
          />
          <div class="invalid-feedback">{{ errors.tanggal_mulai }}</div>
        </div>
        <div class="form-group col">
          <label>Tanggal Selesai</label>
          <Field
            name="tanggal_selesai"
            type="date"
            class="form-control"
            :class="{ 'is-invalid': errors.tanggal_selesai }"
          />
          <div class="invalid-feedback">{{ errors.tanggal_selesai }}</div>
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
        <router-link to="/semester" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="dataSemester?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="dataSemester?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Kelas: {{ dataSemester.error }}
      </div>
    </div>
  </template>
</template>
