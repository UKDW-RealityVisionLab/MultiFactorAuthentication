<script setup>
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useRoute } from "vue-router";
import { ref } from "vue";
import axios from "axios";

import { useAlertStore } from "@/stores";
import { router } from "@/router";

const alertStore = useAlertStore();
const route = useRoute();
const baseUrl = "http://localhost:3000/kelas";
const kelasss = ref({
  loading: false,
  error: null,
});

const schema = Yup.object().shape({
  kode_kelas: Yup.number().required("Kode Kelas is required"),
  kode_matakuliah: Yup.number().required("Kode Matakuliah is required"),
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
      await addKelas(values);
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
  <template v-if="!(kelasss?.loading || kelasss?.error)">
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
  <template v-if="kelasss?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="kelasss?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Kelas: {{ kelasss.error }}
      </div>
    </div>
  </template>
</template>
