<template>
  <span>
    <span v-if="downCount > 0" class="mr-2">
      <font-awesome-icon icon="exclamation-triangle" />
    </span>
    <span
      :class="{ 'has-badge has-badge-rounded has-badge-danger': downCount > 0 }"
      :data-badge="downCount > 0 ? downCount : undefined"
      v-text="$t('applications.label')"
    />
  </span>
</template>

<script lang="ts" setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { computed, watch } from 'vue';

import faviconDanger from '@/assets/img/favicon-danger.png';
import favicon from '@/assets/img/favicon.png';
import { useApplicationStore } from '@/composables/useApplicationStore';

const { applications } = useApplicationStore();
const downCount = computed(() => {
  return applications.value.reduce((current, next) => {
    return (
      current +
      next.instances.filter((instance) => instance.statusInfo.status !== 'UP')
        .length
    );
  }, 0);
});

watch(downCount, (newVal: number) => {
  updateFavicon(newVal === 0);
});

const updateFavicon = (up) =>
  ((document.querySelector('link[rel*="icon"]') as HTMLLinkElement).href = up
    ? favicon
    : faviconDanger);
</script>
