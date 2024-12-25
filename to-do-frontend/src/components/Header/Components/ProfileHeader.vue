<script setup>
import { ref } from 'vue';
import { useAuthStore } from "../../../stores/AuthStore.js";
import { useUsersStore } from "../../../stores/UsersStore.js";
import router from "../../../router.js";

let userInformation = ref(await useUsersStore().getUser);

async function logOut() {
  await useAuthStore().logOut();
  console.log("User logged out");
  await router.push({ name: "login" });
  console.log("login was pushed")
}
</script>

<template>
  <button class="profile-container d-flex justify-content-center align-items-center dropdown-toggle  "
          data-bs-toggle="dropdown"
          aria-expanded="false"
  >
    <span class="profile-name">{{ userInformation.email }}</span>
  </button>
  <ul class="dropdown-menu dropdown-menu-end">
    <li>
      <router-link to="/profile" class="nav-link">Profile</router-link>
    </li>
    <li @click="logOut">
      <span>Log Out</span>
    </li>
  </ul>
</template>

<style scoped>

.profile-container {
  padding: 5px;
  background: none;
  outline: none;
  cursor: pointer;
  box-shadow: none;
  border: none;
  border-radius: 5px;
  &:hover {
    background: #5bc4a7;
  }
}
.avatar-image {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-name {
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  max-width: 100px;
  color: white;
}
</style>
