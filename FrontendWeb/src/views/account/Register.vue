<script setup>
import { Form, Field } from 'vee-validate';
import * as Yup from 'yup';
import { useApp } from '../../stores/app.store.js';
import axios from "axios";
import { useUsersStore, useAlertStore } from '@/stores';
import { router } from '@/router';
// import {path} from '../../router/account.routes'

const alertStore = useAlertStore();


const schema = Yup.object().shape({
    firstName: Yup.string().required('First Name is required'),
    lastName: Yup.string().required('Last Name is required'),
    username: Yup.string().required('Username is required'),
    password: Yup.string().required('Password is required').min(6, 'Password must be at least 6 characters')
});

const addDosen = async (data) => {
    const app = useApp();
    await app.addData(data,"/dosen");
    window.open(router.resolve(`/account/login`).href,'_self');
    // alertStore.success(response.data.message);
};

async function onSubmit(values) {
    const usersStore = useUsersStore();
    const alertStore = useAlertStore();
    let nama = values.firstName + ' ' + values.lastName;
        const newDosen = {
            nidn: values.nidn,
            kode_prodi: values.kodeProdi,
            nama: nama,
        };
        addDosen(newDosen);
    // const localUser={
    //  role: 'dosen',
    //  firstName: values.firstName,
    //  lastName: values.lastName,
    //  username: values.nama,
    //  password: values.password,
    // }
    await usersStore.register(values);
    // localStorage.setItem("mfa-feapps", 
    alertStore.success("user added");
    }
</script>



<template>
  <h1>Add Dosen</h1>
  <template v-if="!(dataDosen?.loading || dataDosen?.error)">
    <Form
      @submit="onSubmit"
      :validation-schema="schema"
      v-slot="{ errors, isSubmitting }"
    >
      <div class="form-row">
        <div class="form-group col">
          <label>NIDN</label>
          <Field
            name="nidn"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.nidn }"
          />
          <div class="invalid-feedback">{{ errors.nidn }}</div>
        </div>
        <div class="form-group col">
          <label>Kode Prodi</label>
          <Field
            name="kodeProdi"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.kodeProdi }"
          />
          <div class="invalid-feedback">{{ errors.kodeProdi }}</div>
        </div>
        <div class="form-group col">
          <label>First Name</label>
          <Field
            name="firstName"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.firstName }"
          />
          <div class="invalid-feedback">{{ errors.firstName }}</div>
        </div>
        <div class="form-group col">
          <label>Last Name</label>
          <Field
            name="lastName"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.lastName }"
          />
          <div class="invalid-feedback">{{ errors.lastName }}</div>
        </div>
        </div>
        <div class="form-group col">
          <label>Username</label>
          <Field
            name="username"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': errors.username }"
          />
          <div class="invalid-feedback">{{ errors.username }}</div>
        </div>
        <div class="form-group col">
          <label>Password</label>
          <Field
            name="password"
            type="password"
            class="form-control"
            :class="{ 'is-invalid': errors.password }"
          />
          <div class="invalid-feedback">{{ errors.password }}</div>
        </div>
      <div class="form-group">
        <button class="btn btn-primary" :disabled="isSubmitting">
          <span
            v-show="isSubmitting"
            class="spinner-border spinner-border-sm mr-1"
          ></span>
          Register
        </button>
        <router-link to="/Login" class="btn btn-link">Cancel</router-link>
      </div>
    </Form>
  </template>
  <template v-if="dataDosen?.loading">
    <div class="text-center m-5">
      <span class="spinner-border spinner-border-lg align-center"></span>
    </div>
  </template>
  <template v-if="dataDosen?.error">
    <div class="text-center m-5">
      <div class="text-danger">
        Error loading Registrasi: {{ dataDosen.error }}
      </div>
    </div>
  </template>
</template>