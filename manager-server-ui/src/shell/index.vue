<template>
  <div id="shell">
    <SmNavbar />
    <div class="pt-14 h-full">
      <router-view :applications="applications" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useApplicationStore } from '@/composables/useApplicationStore';
import SmNavbar from '@/shell/navbar.vue';

const { applications } = useApplicationStore();
</script>
