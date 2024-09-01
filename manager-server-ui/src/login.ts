import { createApp } from 'vue';
import Login from '@/views/login/login.vue';

const app = createApp(Login, {
  icon: './assets/img/favicon.png',
  title: window.uiSettings?.title ? window.uiSettings.title : 'Server Manager',
});

app.mount('#login');
