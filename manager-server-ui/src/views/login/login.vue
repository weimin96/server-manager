<template>
  <div class="flex content-center items-center justify-center h-full">
    <div class="w-full px-4">
      <div
        class="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-gray-200 border-0"
      >
        <div class="flex items-center">
          <img
            v-if="icon"
            :src="icon"
            class="flex-initial w-12 h-12 m-6"
          />
          <div class="flex-initial text-2xl font-semibold">{{ title }}</div>
        </div>
        <div class="flex-auto px-4 py-10 pt-0">
          <form>
            <div class="relative w-full mb-3">
              <label
                class="block uppercase text-gray-600 text-sm font-bold text-justify mb-2"
                htmlFor="grid-password"
              >
                用户名
              </label>
              <input
                v-model="username"
                type="text"
                class="border-0 px-3 py-3 placeholder-gray-300 text-gray-600 bg-white rounded text-sm shadow focus:outline-none focus:ring-blue-500 w-full ease-linear transition-all duration-150"
                placeholder="请输入用户名"
                autofocus
              />
            </div>
            <div class="relative w-full mb-3">
              <label
                class="block uppercase text-gray-600 text-sm font-bold mb-2"
                htmlFor="grid-password"
              >
                密码
              </label>
              <input
                v-model="password"
                type="password"
                class="border-0 px-3 py-3 placeholder-gray-300 text-gray-600 bg-white rounded text-sm shadow focus:outline-none focus:ring-blue-500 w-full ease-linear transition-all duration-150"
                placeholder="请输入密码"
                @keyup.enter="login"
              />
            </div>
            <div class="text-center mt-6 w-96">
              <button
                class="bg-gray-800 text-white active:bg-gray-600 text-sm font-bold uppercase px-6 py-3 rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 w-full ease-linear transition-all duration-150"
                type="button"
                @click="login"
              >
                登录
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

import { setCurrentUser } from '@/main/config';
import User from '@/services/user';

export default defineComponent({
  props: {
    icon: {
      type: String,
      default: undefined,
    },
    title: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      username: '',
      password: '',
    };
  },
  methods: {
    login(event) {
      event.preventDefault();
      User.login(this.username, this.password)
        .then((response) => {
          // 成功存储用户信息和设置请求头
          localStorage.setItem('token', response.data);
          setCurrentUser(this.username);
          let url = window.location.href.replace('login.html', 'applications');
          url = url.replace('login', 'applications');
          window.location.href = url;
        })
        .catch((error) => {
          ElMessage.error(error.response.data);
          // 失败提示
          localStorage.removeItem('token');
        });
    },
  },
});
</script>
