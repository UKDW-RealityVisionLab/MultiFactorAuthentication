<script setup>
import { useApp } from '../../stores/app.store.js';
import path from '../../router/semester.routes';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter, useRoute } from "vue-router"; 
import { ref, onMounted } from 'vue';
import { fetchWrapper } from '@/helpers';
const alertStore = useAlertStore();
const router = useRouter();
const route = useRoute();
const kodeSemester = route.params.kode_semester;

const dataSemester = ref({
  dataId: [],
  loading: false,
  error: null,
});

const schema = Yup.object().shape({
  tahun_ajaran: Yup.string().required("Tahun ajaran is required"),
  tanggal_mulai: Yup.date().required("Tanggal mulai is required"),
  tanggal_selesai: Yup.date().required("Tanggal selesai is required")
});
 
const fetchDataSemester = async () => {
  dataSemester.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getDataById(path.path, kodeSemester);
    dataSemester.value.dataId = response; 
    console.log("Data yang didapat:", dataSemester.value.dataId); 
  } catch (error) {
    dataSemester.value.error = error.message;
  } finally {
    dataSemester.value.loading = false;
  }
};

onMounted(() => {
  if (kodeSemester) {
    fetchDataSemester();
  }
});

async function onSubmit(values) {
    const alertStore = useAlertStore();
    const app = useApp();
    try {
        dataSemester.value.loading = true;
        const data = {
            tahun_ajaran: values.tahun_ajaran,
            tanggal_mulai: values.tanggal_mulai,
            tanggal_selesai: values.tanggal_selesai,
        };
        dataSemester.value.loading = true;
        console.log(path);
        await app.editData(path.path, kodeSemester, data);
        await router.push(path.path);
        alertStore.success("Semester update successfully");
    } catch (error) {
        alertStore.error(error.message || "Failed to update");
    } finally {
        dataSemester.value.loading = false;
    }
};
</script>

<template>
  <h1>Edit Semester</h1>
  <template v-if="!dataSemester.loading && !dataSemester.error">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-for="data in dataSemester.dataId"
      :initial-values="data"
      v-slot="{ errors, isSubmitting }"
    >
      <div class="form-row" :key="data.kode_semester">
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
        Error loading data: {{ dataSemester.error }}
      </div>
    </div>
  </template>
</template>
