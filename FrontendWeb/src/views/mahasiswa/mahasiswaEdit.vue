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
const nim = route.params.nim; // Corrected the variable name
const baseUrl = "http://localhost:3000/users";
const mahasiswaData = ref({
    dataId: [],
    loading: false,
    error: null,
});

const fetchMahasiswaById = async (url) => {
    try {
        const response = await axios.get(url);
        mahasiswaData.value.dataId = response.data;
        console.log('Data by Nim:', mahasiswaData.value.dataId);
    } catch (error) {
        alertStore.error("Failed to fetch kelas data");
    }
};

if (nim) {
    onMounted(async () => {
        try {
            await fetchMahasiswaById(`${baseUrl}/${nim}`);
        } catch (error) {
            alertStore.error("Failed to fetch data mahasiswa");
        }
    });
}

const editMahasiswa = async (values) => {
    let message;
    mahasiswaData.value.loading = true;
    try {
        const data = {
            kode_prodi: values.kode_prodi,
            tahun_angkatan: values.tahun_angkatan,
            nama: values.nama,
        };
        console.log("Data yang disend request:", data);
        await axios.patch(`${baseUrl}/${nim}`, data);

        message = "Mahasiswa updated=" + nim;
        alertStore.success(message);
    } catch (error) {
        if (error.response && error.response.status === 500) {
            console.error("Internal Server Error:", error.message);
        } else {
            console.error("Failed to update mahasiswa:", error.message);
        }
        alertStore.error("Failed to update mahasiswa");
        mahasiswaData.value.error = error.message;
    } finally {
        mahasiswaData.value.loading = false;
    }
};

async function onSubmit(values) {
    try {
        let message;
        if (nim) {
            try {
                await editMahasiswa(values);
                console.log(values);
                await router.push("/mahasiswa");
                message = "Kelas updated kode kuliah=" + nim;
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
  <h1>Edit Mahasiswa</h1>
  <template v-if="!(mahasiswaData?.loading || mahasiswaData?.error)">
    <Form
        @submit="onSubmit"
        :validation-schema="schema"
        v-for="data in mahasiswaData.dataId"
        :initial-values="data"
        v-slot="{ errors, isSubmitting }"
      >
      <div class="form-row" v-for="mah in mahasiswaData.dataId" :key="mah.nim" :initial-values="mah">
        <div class="form-group col">
          <label>Kode Prodi</label>
          <Field
            name="kode_prodi"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kode_prodi }"
          />
          <div class="invalid-feedback">{{ errors.kode_prodi }}</div>
        </div>
        <div class="form-group col">
          <label>Tahun Angkatan</label>
          <Field
            name="tahun_angkatan"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.tahun_angkatan }"
          />
          <div class="invalid-feedback">{{ errors.tahun_angkatan }}</div>
        </div>
        <div class="form-group col">
          <label>Nama</label>
          <Field
            name="nama"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.nama }"
          />
          <div class="invalid-feedback">{{ errors.nama }}</div>
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
        <router-link to="/mahasiswa" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="mahasiswaData?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="mahasiswaData?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Kelas: {{ mahasiswaData.error }}
      </div>
    </div>
  </template>
</template>
