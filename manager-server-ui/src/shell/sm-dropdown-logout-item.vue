<template>
  <div class="w-32">
    <font-awesome-icon class="w-4 pl-4 pr-2" icon="sign-out-alt" />
    <button @click="logout">
      注销
    </button>
  </div>
</template>

<script lang="ts" setup>
import SmConfig from '@/config';
import uri from "@/utils/uri";

const readCookie = (name) => {
  const match = document.cookie.match(
    new RegExp('(^|;\\s*)(' + name + ')=([^;]*)'),
  );
  return match ? decodeURIComponent(match[3]) : null;
};

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('currentUser');
  window.location.href = uri`login`;
}
</script>
