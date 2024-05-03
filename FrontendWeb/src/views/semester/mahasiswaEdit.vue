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
const kode_semester = route.params.kode_semester; 
const baseUrl = "http://localhost:3000/semester";
const semesterData = ref({
    dataId: [],
    loading: false,
    error: null,
});

const schema = Yup.object().shape({
  tahun_ajaran: Yup.string().required("Tahun ajaran is required"),
  tanggal_mulai: Yup.date(),
  tanggal_selesai: Yup.date(),
});

const fetchSemesterById = async (url) => {
    try {
        const response = await axios.get(url);
        semesterData.value.dataId = response.data.dataSemester.Semester;
        console.log('Data by Nim:', semesterData.value.dataId);
    } catch (error) {
        alertStore.error("Failed to fetch semester data");
    }
};

if (kode_semester) {
    onMounted(async () => {
        try {
            await fetchSemesterById(`${baseUrl}/${kode_semester}`);
        } catch (error) {
            alertStore.error("Failed to fetch data semester");
        }
    });
}

const editSemester = async (values) => {
    let message;
    semesterData.value.loading = true;
    try {
        const data = {
            tahun_ajaran: values.tahun_ajaran,
            tanggal_mulai: values.tanggal_mulai,
            tanggal_selesai: values.tanggal_selesai,
        };
        console.log("Data yang disend request:", data);
        await axios.patch(`${baseUrl}/${kode_semester}`, data);

        message = "Semester updated=" + kode_semester;
        alertStore.success(message);
    } catch (error) {
        if (error.response && error.response.status === 500) {
            console.error("Internal Server Error:", error.message);
        } else {
            console.error("Failed to update data semester:", error.message);
        }
        alertStore.error("Failed to update data semester");
        semesterData.value.error = error.message;
    } finally {
        semesterData.value.loading = false;
    }
};

async function onSubmit(values) {
    try {
        let message;
        if (kode_semester) {
            try {
                await editSemester(values);
                console.log(values);
                await router.push("/semester");
                message = "Semester updated kode semester=" + kode_semester;
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
  <h1>Edit Semester</h1>
  <template v-if="!(semesterData?.loading || semesterData?.error)">
    <Form
        @submit="onSubmit"
        :validation-schema="schema"
        v-for="data in semesterData.dataId"
        :initial-values="data"
        v-slot="{ errors, isSubmitting }"
      >
      <div class="form-row" v-for="mah in semesterData.dataId" :key="mah.kode_semester" :initial-values="mah">
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
        <router-link to="/mahasiswa" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="semesterData?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="semesterData?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Kelas: {{ semesterData.error }}
      </div>
    </div>
  </template>
</template>
