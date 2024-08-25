<template>
  <a
    v-if="href"
    :href="href"
    :target="target"
    class="sm-nav-item"
    rel="noopener noreferrer"
  >
    <slot />
  </a>
  <router-link v-else-if="to" :to="to" class="sm-nav-item">
    <slot />
  </router-link>

  <div v-else class="sm-nav-item">
    <slot />
  </div>
</template>

<script lang="ts" setup>
import { PropType } from 'vue';
import { RouteLocationRaw } from 'vue-router';

defineProps({
  href: {
    type: String,
    default: null,
  },
  target: {
    type: String,
    default: '_blank',
  },
  to: {
    type: Object as PropType<RouteLocationRaw>,
    default: null,
  },
});
</script>
