<template>
  <Suspense>
    <template #default>
      <div v-if="!isLoaded">
        <Header/>
        <div class="loader">
          <span></span>
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
      <div v-else>
        <Header/>
        <router-view/>
      </div>
    </template>
    <template #fallback>
      <div>Something went wrong</div>
    </template>
  </Suspense>
</template>

<script setup>

import {onBeforeMount, onMounted, ref, watch} from 'vue';
import { useAuthStore } from './stores/AuthStore.js';
import Header from './components/Header/Header.vue';
import router from "./router.js";

let isLoaded = ref(false);

const authStore = useAuthStore();

watch(() => authStore.getHasInitialization, () => {
  isLoaded.value = authStore.getHasInitialization;
})

/*import {onBeforeMount, onMounted, ref, watch} from 'vue';
import { useAuthStore } from './stores/AuthStore.js';
import Header from './components/Header/Header.vue';
import router from "./router.js";

let isLoaded = ref(false);

watch(() => useAuthStore.getHasInitialization, () => {
  console.log('12312312312332');
  isLoaded.value = useAuthStore.getHasInitialization;
  console.log('isLoaded', isLoaded.value);
})*/

/*onBeforeMount(async () => {
  console.log('Before loading');
  isLoaded.value = await useAuthStore().authorization();
  console.log('After loading ');

  await router.push("/log-in");
  console.log('After loading');
});*/
</script>

<style src="./css/main.scss"></style>
