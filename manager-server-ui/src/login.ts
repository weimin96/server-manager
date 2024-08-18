import { createApp } from 'vue';

import './login.css';

import i18n from './i18n';
import Login from './login/login.vue';

const app = createApp(Login, {
  csrf: window.csrf,
  icon: window.uiSettings.icon,
  title: window.uiSettings.title,
  theme: window.uiSettings.theme,
  param: window.param,
});
app.use(i18n);
app.mount('#login');
