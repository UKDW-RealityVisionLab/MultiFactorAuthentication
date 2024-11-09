<script setup>
import { ref } from 'vue';
import { useApp } from '../../stores/app.store.js';
import path from '../../router/ruang.router';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter } from 'vue-router';

const router = useRouter();

const ruang = ref({
  loading: false,
  error: null,
});

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


async function onSubmit(values) {
  const alertStore = useAlertStore();
  const app = useApp();
  ruang.value.loading = true;

  try {
    const newRuang = {
      kodeRuang: values.kodeRuang,
      nama: values.nama,
      latitude: values.latitude, 
      longitude: values.longitude, 
    };

    await app.addData(newRuang, path.path);
    alertStore.success("Ruang added successfully");
    await router.push(path.path);

  } catch (error) {
    ruang.value.error = "Failed to add ruang. Please try again.";
    alertStore.error("Failed to add ruang");
  } finally {
    ruang.value.loading = false;
  }
};
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
        <router-link to="/ruang" class="btn btn-link">Cancel</router-link>
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
