<template>
  <form class="w-5/6 md:1/2 max-w-lg">
    <sm-panel>
      <input
        v-if="csrf"
        :name="csrf.parameterName"
        :value="csrf.token"
        type="hidden"
      />
      <div class="flex text-lg pb-3 items-center">
        <img v-if="icon" :src="icon" class="w-8 h-8 mr-2" />
        <h1 class="title has-text-primary" v-text="title" />
      </div>
      <div class="relative border-t -ml-4 -mr-4 overflow-hidden">
        <sm-wave class="bg-wave--login" />
        <div class="ml-4 mr-4 pt-2 z-10 relative">
          <sm-alert :error="message" />
          <sm-alert :error="logout" :severity="Severity.INFO" />
          <div :class="{ 'has-errors': error }" class="pb-4 form-group">
            <sm-input
              v-model="username"
              :label="t('login.placeholder.username')"
              autocomplete="username"
              name="username"
              type="text"
              autofocus
            />
            <sm-input
              v-model="password"
              :label="t('login.placeholder.password')"
              autocomplete="current-password"
              name="password"
              type="password"
            />
            <sm-checkbox
              v-if="rememberMeEnabled"
              :label="t('login.remember_me')"
              class="justify-end"
              name="remember-me"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="text-right">
          <sm-button @click="login"> 登录 </sm-button>
        </div>
      </template>
    </sm-panel>
  </form>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import SmAlert, { Severity } from '@/components/sm-alert';
import SmButton from '@/components/sm-button';
import SmCheckbox from '@/components/sm-checkbox';
import SmInput from '@/components/sm-input';
import SmPanel from '@/components/sm-panel';
import SmWave from '@/components/sm-wave';

import { setCurrentUser } from '@/config';
import User from '@/services/user';

const i18n = useI18n();
const t = i18n.t;

const props = defineProps({
  param: {
    type: Object,
    default: () => ({}),
  },
  icon: {
    type: String,
    default: undefined,
  },
  title: {
    type: String,
    required: true,
  },
  csrf: {
    type: Object,
    default: undefined,
  },
  theme: {
    type: Object,
    default: undefined,
  },
});

const { rememberMeEnabled } = window.uiSettings;

const username = ref('');
const password = ref('');
const message = ref(null);

const login = (event) => {
  event.preventDefault();
  User.login(username.value, password.value)
    .then((response) => {
      // 成功存储用户信息和设置请求头
      localStorage.setItem('token', response.data);
      setCurrentUser(username.value);
      let url = window.location.href.replace('login.html', 'applications');
      url = url.replace('login', 'applications');
      window.location.href = url;
    })
    .catch((error) => {
      // 失败提示
      message.value = error.response.data;
      localStorage.removeItem('token');
      setTimeout(() => {
        message.value = null;
      }, 3000);
    });
};

const logout = computed(() => {
  return props.param.logout !== undefined
    ? t('login.logout_successful')
    : undefined;
});
</script>

<style scoped>
.bg-wave--login {
  @apply z-0 absolute left-0;
  min-width: 100%;
  height: 4rem;
}

.form-group {
  @apply grid grid-cols-1 gap-2;
}

.form-group.has-errors label {
  @apply text-red-500;
}

.form-group.has-errors input {
  @apply focus:ring-red-500 focus:border-red-500 border-red-400;
}
</style>
