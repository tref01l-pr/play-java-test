import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { fab } from '@fortawesome/free-brands-svg-icons';
import 'bootstrap';
import './assets/main.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import router from './router';


const pinia = createPinia();
library.add(fas, fab);

createApp(App)
    .use(router)
    .use(pinia)
    .component('fa', FontAwesomeIcon)
    .mount('#app');