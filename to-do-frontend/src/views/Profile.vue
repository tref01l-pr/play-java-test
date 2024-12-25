<script setup>
import { computed } from "vue";
import { useUsersStore } from "../stores/UsersStore.js";

const userInformation = computed(() => useUsersStore().getUser);
</script>

<template>
  <div>
    <p>Profile</p>
    <p>id: {{userInformation.id}}</p>
    <p>userName: {{userInformation.username}}</p>
    <p>created at: {{userInformation.userCreatedAt}}</p>
  </div>
</template>