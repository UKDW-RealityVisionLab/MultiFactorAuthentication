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
const baseUrl = "http://localhost:3000/ruang";

const schema = Yup.object().shape({
  kodeRuang: Yup.string().required("kode ruang is required"),
  nama: Yup.string().required("nama is required"),
  latitude: Yup.number()
    .required("latitude is required")
    .min(-90, "Latitude must be at least -90")
    .max(90, "Latitude must be at most 90"),
  longitude: Yup.number()
    .required("longitude is required")
    .min(-180, "Longitude must be at least -180")
    .max(180, "Longitude must be at most 180"),
});

const ruang = ref({
  loading: false,
  error: null,
});

const addRuang = async (data) => {
  ruang.value.loading = true; // Fix variable name
  try {
    const response = await axios.post(baseUrl, data);
    alertStore.success(response.data.message);
  } catch (error) {
    ruang.value.error = error.message;
  } finally {
    ruang.value.loading = false;
  }
};

async function onSubmit(values) {
  try {
    const ruangData = {
      kodeRuang: values.kodeRuang,
      nama: values.nama,
      latitude: values.latitude, 
      longitude: values.longitude, 
    };

    await addRuang(ruangData);
    alertStore.success("ruang added");
    await router.push("/ruang");
  } catch (error) {
    alertStore.error("Failed to add ruang");
  }
}
</script>

<template>
  <h1>Add ruang</h1>
  <template v-if="!(ruang.loading || ruang.error)">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-slot="{ errors, isSubmitting }"
    >
      <div class="form-row">
        <div class="form-group col">
          <label>kode ruang</label>
          <Field
            name="kodeRuang"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kodeRuang }"
          />
          <div class="invalid-feedback">{{ errors.kodeRuang }}</div>
        </div>
        <div class="form-row">
        <div class="form-group col">
          <label>Nama ruang</label>
          <Field
            name="nama"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.nama }"
          />
          <div class="invalid-feedback">{{ errors.nama }}</div>
        </div>
        <div class="form-group col">
          <label>Langitude</label>
          <Field
            name="latitude"
            type="number"
            class="form-control"
            :class="{ 'is-invalid': errors.latitude }"
          />
          <div class="invalid-feedback">{{ errors.latitude }}</div>
        </div>
        <div class="form-group col">
          <label>Longitude</label>
          <Field
            name="longitude"
            type="number"
            class="form-control"
            :class="{ 'is-invalid': errors.longitude }"
          />
          <div class="invalid-feedback">{{ errors.longitude }}</div>
        </div>
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
  <template v-if="ruang.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="ruang.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading : {{ ruang.error }}
      </div>
    </div>
  </template>
</template>
