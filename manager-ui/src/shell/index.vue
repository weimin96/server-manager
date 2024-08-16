<template>
  <div id="app">
    <SbaNavbar :error="error" />
    <div class="sba-container">
      <router-view :applications="applications" :error="error" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useApplicationStore } from '@/composables/useApplicationStore';
import SbaNavbar from '@/shell/navbar';

defineProps({
  error: {
    type: Error,
    default: null,
  },
});

const { applications } = useApplicationStore();
</script>

<style scoped>
.sba-container {
  @apply pt-14 h-full;
}
</style>
