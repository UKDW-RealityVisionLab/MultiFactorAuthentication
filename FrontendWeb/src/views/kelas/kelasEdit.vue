<script setup>
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useRoute } from "vue-router";
import { ref, onMounted } from "vue";
import axios from "axios";

import { useAlertStore } from "@/stores";
import { useRouter } from "vue-router";

const alertStore = useAlertStore();
const route = useRoute();
const router = useRouter();
const kode_kelas = route.params.kode_kelas;
const baseUrl = "http://localhost:3000/kelas";
const kelasData = ref({
    dataId: [],
    loading: false,
    error: null,
});

const fetchKelasById = async (url) => {
    try {
        const response = await axios.get(url);
        kelasData.value.dataId = response.data;
        console.log('Data by kode kelas:', kelasData.value.dataId);
    } catch (error) {
        kelasData.value.error = "Failed to fetch kelas data";
        alertStore.error(kelasData.value.error);
    } finally {
        kelasData.value.loading = false;
    }
};

onMounted(() => {
    if (kode_kelas) {
        kelasData.value.loading = true;
        fetchKelasById(`${baseUrl}/${kode_kelas}`);
    }
});

const schema = Yup.object().shape({
    kode_matakuliah: Yup.string(),
    group_kelas: Yup.string(),
    kode_semester: Yup.string(),
    kode_dosen: Yup.number(),
});

const editKelas = async (values) => {
    let message;
    kelasData.value.loading = true;
    try {
        const data = {
            kode_matakuliah: values.kode_matakuliah,
            group_kelas: values.group_kelas,
            kode_semester: values.kode_semester,
            kode_dosen: values.kode_dosen,
        };
        console.log("Data yang disend request:", data);
        await axios.patch(`${baseUrl}/${kode_kelas}`, data);

        message = "Kelas updated=" + kode_kelas;
        alertStore.success(message);
        await router.push("/kelas");
    } catch (error) {
        console.error("Failed to update Kelas:", error.message);
        alertStore.error("Failed to update Kelas");
        kelasData.value.error = error.message;
    } finally {
        kelasData.value.loading = false;
    }
};

async function onSubmit(values) {
    try {
        if (kode_kelas) {
            await editKelas(values);
        }
    } catch (error) {
        alertStore.error(error.message);
    }
}
</script>
<template>
  <h1>Edit Kelas</h1>
  <template v-if="!(kelasData.loading || kelasData.error)">
      <Form
          @submit="onSubmit"
          :validation-schema="schema"
          v-slot="{ errors, isSubmitting }"
          v-for="data in kelasData.dataId"
          :key="data.kode_kelas"
          :initial-values="data"
          
      >
          <div class="form-row">
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
                  <span v-show="isSubmitting" class="spinner-border spinner-border-sm mr-1"></span>
                  Save
              </button>
              <router-link to="/kelas" class="btn btn-link">Cancel</router-link>
          </div>
      </Form>
  </template>
  <template v-if="kelasData.loading">
      <div class="text-center m-5">
          <span class="spinner-border spinner-border-lg align-center"></span>
      </div>
  </template>
  <template v-if="kelasData.error">
      <div class="text-center m-5">
          <div class="text-danger">
              Error loading Kelas: {{ kelasData.error }}
          </div>
      </div>
  </template>
</template>
