<template>
  <div class="container">
    <div class="form-body row">
      <div class="col-sm-9 col-md-7 col-lg-5 mx-auto">
        <div class="card border-0 shadow rounded-3 my-5">
          <div class="card-body p-4 p-sm-5">
            <h5 class="card-title text-center mb-5 fw-light fs-5">Log In</h5>
            <form @submit.prevent="submitForm">

              <LogInWithUsername
                  :username="username"
                  :password="password"
                  @update:username="username = $event"
                  @update:password="password = $event"
              />


              <div class="d-grid mt-5">
                <button class="btn btn-primary btn-login text-uppercase fw-bold" type="submit">Log
                  in</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";
import usersService from "../services/usersService.js";
import { useRouter } from "vue-router";
import LogInWithUsername from "../components/Main/LogInViewComponents/LogInWithUsername.vue";
import { useJwtStore } from "../stores/JwtStore.js";
import { useAuthStore } from "../stores/AuthStore.js";

const router = useRouter();
let username = ref("");
let password = ref("");

console.log("login page");

watch(username, (newValue, oldValue) => {
  console.log("username", newValue, oldValue);
});

watch(password, (newValue, oldValue) => {
  console.log("password", newValue, oldValue);
});

function resetInputs() {
  username.value = "";
  password.value = "";
}
async function submitForm(event) {
  event.preventDefault();
  if (!password.value) {
    alert("Password is required");
    return;
  }

  if (!username.value) {
    alert("Username is required");
    return;
  }
  console.log("username", username.value);
  console.log("password", password.value);
  const data = {
    username: username.value,
    password: password.value,
  };

  try {
    const response = await useAuthStore().login(username.value, password.value);
    await router.push({ name: "todos" });
  } catch (error) {
    console.error("Error:", error);
  }
}

</script>

<style>
.form-body {
  margin-bottom: 250px;
}

.type-of-form {
  color: blue;
}

.active-form {
  font-weight: bold;
  text-decoration: underline;
}

</style>
