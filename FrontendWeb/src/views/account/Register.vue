<script setup>
import { Form, Field } from 'vee-validate';
import * as Yup from 'yup';
import axios from "axios";
import { useUsersStore, useAlertStore } from '@/stores';
import { router } from '@/router';

const alertStore = useAlertStore();
const baseMhs= 'http://localhost:3000/users'; 
const baseDosen= 'http://localhost:3000/dosen'; 

const schema = Yup.object().shape({
    firstName: Yup.string().required('First Name is required'),
    lastName: Yup.string().required('Last Name is required'),
    username: Yup.string().required('Username is required'),
    password: Yup.string().required('Password is required').min(6, 'Password must be at least 6 characters')
});

const addMhs = async (data) => {
    const response = await axios.post(baseMhs, data);
    alertStore.success(response.data.message);
};

const addDosen = async (data) => {
    const response = await axios.post(baseDosen, data);
    alertStore.success(response.data.message);
};

async function onSubmit(values) {
    const usersStore = useUsersStore();
    const alertStore = useAlertStore();
    let nama = values.firstName + ' ' + values.lastName;

    if (values.role === "mahasiswa") {
        const newMhs = {
            nim: values.nim,
            kode_prodi: values.kodeProdi,
            tahun_angkatan: values.angkatan, 
            nama: nama,
        };
        await addMhs(newMhs);
    } else if (values.role === "dosen") {
        const newDosen = {
            nidn: values.nidn,
            kode_prodi: values.kodeProdi,
            nama: nama,
        };
        await addDosen(newDosen);
    }
    
    await usersStore.register(values);
    alertStore.success("user added");
    await router.push("/account/login");
}
</script>



<template>
    <div class="card m-3">
        <h4 class="card-header">Register</h4>
        <div class="card-body">
            <Form @submit="onSubmit" :validation-schema="schema" v-slot="{ errors, isSubmitting, values }">
                <div class="form-group col">
                    <label>Role</label>
                    <Field name="role" as="select" class="form-control" :class="{ 'is-invalid': errors.role }">
                        <option value="mahasiswa">Mahasiswa</option>
                        <option value="dosen">Dosen</option>
                    </Field>
                    <div class="invalid-feedback">{{ errors.role }}</div>                    
                </div>

                <!-- Form input for Dosen -->
                <div v-if="values.role === 'dosen'">
                    <div class="form-group">
                        <label>NIDN</label>
                        <Field name="nidn" type="text" class="form-control" :class="{ 'is-invalid': errors.nidn }" />
                        <div class="invalid-feedback">{{ errors.nidn }}</div>
                    </div>
                    <div class="form-group">
                        <label>Kode Prodi</label>
                        <Field name="kodeProdi" type="text" class="form-control" :class="{ 'is-invalid': errors.kodeProdi }" />
                        <div class="invalid-feedback">{{ errors.kodeProdi }}</div>
                    </div>
                </div>

                <!-- Form input for Mahasiswa -->
                <div v-else-if="values.role === 'mahasiswa'">
                    <div class="form-group">
                        <label>NIM</label>
                        <Field name="nim" type="text" class="form-control" :class="{ 'is-invalid': errors.nim }" />
                        <div class="invalid-feedback">{{ errors.nim }}</div>
                    </div>
                    <div class="form-group">
                        <label>Kode Prodi</label>
                        <Field name="kodeProdi" type="text" class="form-control" :class="{ 'is-invalid': errors.kodeProdi }" />
                        <div class="invalid-feedback">{{ errors.kodeProdi }}</div>
                    </div>
                    <div class="form-group">
                        <label>Angkatan</label>
                        <Field name="angkatan" type="text" class="form-control" :class="{ 'is-invalid': errors.angkatan }" />
                        <div class="invalid-feedback">{{ errors.angkatan }}</div>
                    </div>
                </div>

                <!-- Common form inputs -->
                <div class="form-group">
                    <label>First Name</label>
                    <Field name="firstName" type="text" class="form-control" :class="{ 'is-invalid': errors.firstName }" />
                    <div class="invalid-feedback">{{ errors.firstName }}</div>
                </div>
                <div class="form-group">
                    <label>Last Name</label>
                    <Field name="lastName" type="text" class="form-control" :class="{ 'is-invalid': errors.lastName }" />
                    <div class="invalid-feedback">{{ errors.lastName }}</div>
                </div>
                <div class="form-group">
                    <label>Username</label>
                    <Field name="username" type="text" class="form-control" :class="{ 'is-invalid': errors.username }" />
                    <div class="invalid-feedback">{{ errors.username }}</div>
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <Field name="password" type="password" class="form-control" :class="{ 'is-invalid': errors.password }" />
                    <div class="invalid-feedback">{{ errors.password }}</div>
                </div>
                <div class="form-group">
                    <button class="btn btn-primary" :disabled="isSubmitting">
                        <span v-show="isSubmitting" class="spinner-border spinner-border-sm mr-1"></span>
                        Register
                    </button>
                    <router-link to="login" class="btn btn-link">Cancel</router-link>
                </div>
            </Form>
        </div>
    </div>
</template>
