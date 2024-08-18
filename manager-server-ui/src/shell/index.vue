<template>
  <div id="app">
    <SmNavbar :error="error" />
    <div class="sm-container">
      <router-view :applications="applications" :error="error" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useApplicationStore } from '@/composables/useApplicationStore';
import SmNavbar from '@/shell/navbar';

defineProps({
  error: {
    type: Error,
    default: null,
  },
});

const { applications } = useApplicationStore();
</script>

<style scoped>
.sm-container {
  @apply pt-14 h-full;
}
</style>
