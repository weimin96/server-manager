<template>
  <div id="app">
    <SmNavbar :error="error" />
    <div class="pt-14 h-full">
      <router-view :applications="applications" :error="error" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useApplicationStore } from '@/composables/useApplicationStore';
import SmNavbar from '@/shell/navbar.vue';

defineProps({
  error: {
    type: Error,
    default: null,
  },
});

const { applications } = useApplicationStore();
</script>
