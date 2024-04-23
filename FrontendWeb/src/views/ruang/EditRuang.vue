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
const kodeRuang = route.params.kode_ruang;
const baseUrl = "http://localhost:3000/ruang";
const ruang = ref({
  data: [],
  loading: false,
  error: null,
});


const fetchRuangById = async (url) => {
  try {
    const response = await axios.get(url)
    ruang.value.data = response.data.ruang.dataRuang;
    console.log('Data by kode ruang:', ruang.value.data)

  } catch (error) {
    alertStore.error("Failed to fetch ruang data");
    console.log(kodeRuang)
  }
};

if (kodeRuang) {
  onMounted(async () => {
    try {
      await fetchRuangById(`${baseUrl}/${kodeRuang}`);
    } catch (error) {
      alertStore.error("Failed to fetch ruang data");
    }
  });
}


const schema = Yup.object().shape({
  nama: Yup.string().required("Nama is required"),
  latitude: Yup.number()
    .required("Latitude is required")
    .min(-90, "Latitude must be at least -90")
    .max(90, "Latitude must be at most 90"),
  longitude: Yup.number()
    .required("Longitude is required")
    .min(-180, "Longitude must be at least -180")
    .max(180, "Longitude must be at most 180"),
});


const editRuang= async (values) => {
  let message;
  ruang.value.loading = true;
  try {
    const data = {
      nama: values.nama,
      latitude: values.latitude, 
      longitude: values.longitude, 
    };
    console.log("Data yang disend request:", data); 
    await axios.patch(`${baseUrl}/${kodeRuang}`, data);
    
    message = "ruang updated kode kuliah="+kodeRuang;
    alertStore.success(message)
  } catch (error) {
    if (error.response && error.response.status === 500) {
      console.error("Internal Server Error:", error.message);
    } else {
      console.error("Failed to update ruang:", error.message);
    }
    alertStore.error("Failed to update ruang");
    ruang.value.error = error.message;
  } finally {
    ruang.value.loading = false;
  }
};

async function onSubmit(values) {
  try {
    let message;
    if (kodeRuang) {
      try {
        await editRuang(values);
        console.log(values);
        await router.push("/ruang");
        message = "Ruang updated kode ="+kodeRuang;
      } catch (error) {
        alertStore.error("Failed to update");
      }
    } 
    alertStore.success(message);
  } catch (error) {
    alertStore.error(error);
  }
}

</script>

<template>
    <h1>Edit Ruang</h1>
    <template v-if="!(ruang?.loading || ruang?.error)">
      <Form @submit="onSubmit" :validation-schema="schema" v-slot="{ errors, isSubmitting }"  v-for="ruangFor in ruang.data" :key="ruangFor.kodeRuang" :initial-values="ruangFor">
        <div class="form-row">
          <div class="form-group col">
            <label>Nama Ruang</label>
            <Field name="nama" type="text" class="form-control"
              :class="{ 'is-invalid': errors.nama }" />
            <div class="invalid-feedback">{{ errors.nama }}</div>
          </div>
          <div class="form-group col">
            <label>Latitude</label>
            <Field name="latitude" type="number" class="form-control" :class="{ 'is-invalid': errors.latitude }" />
            <div class="invalid-feedback">{{ errors.latitude }}</div>
          </div>
          <div class="form-group col">
            <label>Longitude</label>
            <Field name="longitude" type="number" class="form-control" :class="{ 'is-invalid': errors.longitude }" />
            <div class="invalid-feedback">{{ errors.longitude }}</div>
          </div>
        </div>
        <div class="form-group">
          <button class="btn btn-primary" :disabled="isSubmitting">
            <span v-show="isSubmitting" class="spinner-border spinner-border-sm mr-1"></span>
            Save
          </button>
          <router-link to="/ruang" class="btn btn-link">Cancel</router-link>
        </div>
      </Form>
    </template>
    <template v-if="ruang?.loading">
      <div class="text-center m-5">
        <span class="spinner-border spinner-border-lg align-center"></span>
      </div>
    </template>
    <template v-if="ruang?.error">
      <div class="text-center m-5">
        <div class="text-danger">
          Error loading ruang: {{ ruang.error }}
        </div>
      </div>
    </template>
  </template>