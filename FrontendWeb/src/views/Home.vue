<script setup>

</script>

<template>
    <div v-if="user">
        <h1>Hi Dummy!</h1>
        <p>You're logged in with Vue 3 + Pinia & JWT!!</p>
        <p><router-link to="/users">Manage Users</router-link></p>
    </div>
</template>