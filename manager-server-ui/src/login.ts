import { createApp } from 'vue';

import Login from './login/login.vue';

const app = createApp(Login, {
  icon: window.uiSettings.icon,
  title: window.uiSettings.title,
});
// const app = createApp(Login, {
//   csrf: window.csrf,
//   icon: './assets/img/favicon.png',
//   title: 'Server Manager',
// });

app.mount('#login');
