import { createApp } from 'vue';
import { createRouter, createWebHistory } from 'vue-router';

import App from '@/app.vue';
import '@/assets/styles/fonts.css';
import '@/assets/styles/main.css';
import '@/assets/styles/tailwind.css';
import { routes } from '@/routes';

const router = createRouter({
    history: createWebHistory(import.meta.env.VITE_BASE_PUBLIC_PATH),
    routes: routes
});

const app = createApp(App);
app.use(router);
app.mount('#app');
