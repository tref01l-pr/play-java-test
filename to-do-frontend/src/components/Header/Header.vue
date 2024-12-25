<template>
  <nav class="navbar navbar-expand-md navbar-dark fixed-top navbar-background">
    <div class="container-fluid">
      <router-link to="/" class="navbar-brand">ToDoTestProject</router-link>
<!--      <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarCollapse"
          aria-controls="navbarCollapse"
          :aria-expanded="isExpanded ? 'true' : 'false'"
          aria-label="Toggle navigation"
          @click="toggleMenu"
      >
        <span class="navbar-toggler-icon"></span>
      </button>-->
      <div v-if="isAuth" ref="navbarCollapse" class="collapse navbar-collapse" id="navbarCollapse">
        <ul class="navbar-nav me-auto mb-2 mb-md-0">
          <li class="nav-item">
            <router-link to="/profile" class="nav-link" active-class="active">Profile</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/" class="nav-link" active-class="active" exact>ToDos</router-link>
          </li>
        </ul>
      </div>
      <div v-if="!isAuth">
        <button class="btn btn-register">
          <router-link to="/registration" class="nav-link" exact>Sign Up</router-link>
        </button>
        <button class="btn btn-login me-2">
          <router-link to="/log-in" class="nav-link" exact>Sign In</router-link>
        </button>

      </div>
      <div v-else>
        <ProfileHeader/>
      </div>
    </div>
  </nav>
</template>


<script setup>
import {ref, watch} from 'vue';
import {useRoute} from 'vue-router';
import {useUsersStore} from "../../stores/UsersStore.js";
import ProfileHeader from "./Components/ProfileHeader.vue";
import {useAuthStore} from "../../stores/AuthStore.js";

const usersStore = useUsersStore();
const route = useRoute();
const isExpanded = ref(false);
const navbarCollapse = ref(null);
const isAuth = ref(usersStore.isUserLoggedIn);
let isSystemAdmin = ref(usersStore.isSystemAdmin);



watch(() => usersStore.isUserLoggedIn, () => {
  isAuth.value = usersStore.isUserLoggedIn;
})

watch(() => usersStore.isSystemAdmin, () => {
  isSystemAdmin.value = usersStore.isSystemAdmin;
})

const toggleMenu = () => {
  isExpanded.value = !isExpanded.value;
};

/*watch(() => route.path, () => {
  isExpanded.value = false;
  if (navbarCollapse.value.classList.contains('show')) {
    navbarCollapse.value.classList.remove('show');
  }
});*/
</script>

<style lang="scss">
$green: #418b76;
$purple: #51418b;
$red: #8b4156;
$brown: #7b8b41;

.navbar-background {
  background-color: #418b76 !important;
}

.btn-login {
  background-color: #51418b !important;
  color: white !important;
}

.btn-login:hover {
  background-color: rgb(109, 97, 183) !important;
  color: white !important;
}

.btn-register {
  background-color: #8b4156 !important;
  color: white !important;
}

.btn-register:hover {
  background-color: #ad536c !important;
  color: white !important;
}

.bd-placeholder-img {
  font-size: 1.125rem;
  text-anchor: middle;
  -webkit-user-select: none;
  -moz-user-select: none;
  user-select: none;
}

.nav {
  display: flex;
  flex-wrap: nowrap;
  padding-bottom: 1rem;
  margin-top: -1px;
  overflow-x: auto;
  text-align: center;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
}

.active {
  display: block !important;
}
</style>
