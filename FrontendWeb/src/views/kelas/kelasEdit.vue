<script setup>
import { useApp } from '../../stores/app.store.js';
import path from '../../router/kelas.routes';
import { Form, Field } from "vee-validate";
import * as Yup from "yup";
import { useAlertStore } from "@/stores";
import { useRouter, useRoute } from "vue-router"; 
import { ref, onMounted } from 'vue';
import { fetchWrapper } from '@/helpers';
const alertStore = useAlertStore();
const router = useRouter();
const route = useRoute();
const kodeKelas = route.params.kode_kelas;

const kelasData = ref({
    dataId: [],
    loading: false,
    error: null,
});

const schema = Yup.object().shape({
    kode_matakuliah: Yup.string(),
    group_kelas: Yup.string(),
    kode_semester: Yup.string(),
    kode_dosen: Yup.number(),
});

const fetchDataKelas = async () => {
  kelasData.value.loading = true;
  try {
    const app = useApp();
    const response = await app.getDataById(path.path, kodeKelas);
    kelasData.value.dataId = response; 
    console.log("Data yang didapat:", kelasData.value.dataId); 
  } catch (error) {
    kelasData.value.error = error.message;
  } finally {
    kelasData.value.loading = false;
  }
};

onMounted(() => {
  if (kodeKelas) {
    fetchDataKelas();
  }
});

async function onSubmit(values) {
    const alertStore = useAlertStore();
    const app = useApp();
    try {
        kelasData.value.loading = true;
        const data = {
            kode_matakuliah: values.kodeMatakuliah,
            group_kelas: values.grup,
            kode_semester: values.semester,
            kode_dosen: values.kodeDosen,
        };
        kelasData.value.loading = true;
        console.log(path);
        await app.editData(path.path, kodeKelas, data);
        await router.push(path.path);
        alertStore.success("Kelas update successfully");
    } catch (error) {
        alertStore.error(error.message || "Failed to update");
    } finally {
        kelasData.value.loading = false;
    }
};
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
                      name="kodeMatakuliah"
                      type="text"
                      class="form-control"
                      :class="{ 'is-invalid': errors.kodeMatakuliah }"
                  />
                  <div class="invalid-feedback">{{ errors.kodeMatakuliah }}</div>
              </div>
              <div class="form-group col">
                  <label>Group Kelas</label>
                  <Field
                      name="grup"
                      type="text"
                      class="form-control"
                      :class="{ 'is-invalid': errors.grup }"
                  />
                  <div class="invalid-feedback">{{ errors.grup }}</div>
              </div>
              <div class="form-group col">
                  <label>Kode Semester</label>
                  <Field
                      name="semester"
                      type="text"
                      class="form-control"
                      :class="{ 'is-invalid': errors.semester }"
                  />
                  <div class="invalid-feedback">{{ errors.semester }}</div>
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
