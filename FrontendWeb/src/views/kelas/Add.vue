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
const kode_kelas = route.params.kode_kelas;
const baseUrl = "http://localhost:3000/kelas";
const kelasss = ref({
  loading: false,
  error: null,
});


const schema = Yup.object().shape({
  // kodeMataKuliah: Yup.string().required("Kode Mata Kuliah is required"),
  kode_kelas: Yup.number(),
  kode_matakuliah: Yup.number(),
    // .required("SKS is required")
  group_kelas: Yup.string(),
  kode_semester: Yup.number(),
  kode_dosen: Yup.number(),
});

const addKelas = async (data) => {
  kelasss.value.loading = true;
  try {
    const response = await axios.post(baseUrl, data);
    alertStore.success(response.data.message);
  } catch (error) {
    kelasss.value.error = error.message;
  } finally {
    kelasss.value.loading = false;
  }
};


async function onSubmit(values) {
  try {
    let message;
    try {
        const newKelas = {
          kode_kelas: values.kodeKelas,
          kode_matakuliah: values.kodeMataKuliah,
          group_kelas: values.GroupKelas,
          kode_semester: values.kodeSemester,
          kode_dosen: values.kodeDosen,
        };

        await addKelas(newKelas);
        message = "Kelas added";
        await router.push("/kelas");
      } catch (error) {
        alertStore.error("Failed to add Kelas");
      }
    alertStore.success(message);
  } catch (error) {
    alertStore.error(error);
  }
}
</script>

<template>
  <h1>Add Kelas</h1>
  <template v-if="!(kelas?.loading || kelas?.error)">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-slot="{ errors, isSubmitting }"
    >
      <div class="form-row">
        <div class="form-group col">
          <label>Kode Kelas</label>
          <Field
            name="kodeKelas"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kodeKelas }"
          />
          <div class="invalid-feedback">{{ errors.kodeKelas }}</div>
        </div>
        <div class="form-group col">
          <label>Kode Matakuliah</label>
          <Field
            name="kodeMataKuliah"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kodeMataKuliah }"
          />
          <div class="invalid-feedback">{{ errors.kodeMataKuliah }}</div>
        </div>
        <div class="form-group col">
          <label>Group Kelas</label>
          <Field
            name="GroupKelas"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.GroupKelas }"
          />
          <div class="invalid-feedback">{{ errors.GroupKelas }}</div>
        </div>
        <div class="form-group col">
          <label>Kode Semester</label>
          <Field
            name="kodeSemester"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kodeSemester }"
          />
          <div class="invalid-feedback">{{ errors.kodeSemester }}</div>
        </div>
        <div class="form-group col">
          <label>Kode Dosen</label>
          <Field
            name="kodeDosen"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kodeDosen }"
          />
          <div class="invalid-feedback">{{ errors.kodeDosen }}</div>
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
  <template v-if="kelass?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="kelass?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Kelas: {{ kelass.error }}
      </div>
    </div>
  </template>
</template>
