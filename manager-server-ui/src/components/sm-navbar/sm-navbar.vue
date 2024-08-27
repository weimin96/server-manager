<template>
  <nav id="navigation" class="sm-navbar">
    <sm-navbar-brand :brand="brand" />
    <sm-navbar-toggle @click="toggleNavigation" />

    <div
      :class="{ 'nav-container--hidden': !showNavigation }"
      class="nav-container"
    >
      <slot />
    </div>
  </nav>
</template>

<script lang="ts" setup>
import { ref } from 'vue';

import SmNavbarBrand from '@/components/sm-navbar/sm-navbar-brand.vue';
import SmNavbarToggle from '@/components/sm-navbar/sm-navbar-toggle.vue';

defineProps({
  brand: {
    type: String,
    required: true,
  },
});

const showNavigation = ref(false);

function toggleNavigation() {
  showNavigation.value = !showNavigation.value;
}
</script>

<style scoped>
.sm-navbar {
  @apply bg-black fixed flex flex-wrap justify-start mx-auto top-0 w-full z-50 items-center px-4 py-2 text-white;
  @apply sm:px-6;
  @apply xl:px-8;
}
.nav-container {
  @apply flex-grow block w-full flex-row mt-4;
  @apply lg:w-auto lg:flex xl:mt-0;
}
.nav-container--hidden {
  @apply hidden;
  @apply xl:flex;
}
</style>
