<template>
  <div class="hidden md:flex mr-1 gap-1">
    <sm-tag
      :label="$t('applications.applications')"
      :value="applicationsCount"
    />
    <sm-tag :label="$t('applications.instances')" :value="instancesCount" />
  </div>
</template>

<script setup>
import { computed } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';

const { applications } = useApplicationStore();
const applicationsCount = computed({
  get() {
    return applications.value.length;
  },
});

const instancesCount = computed({
  get() {
    return applications.value.reduce(
      (current, next) => current + next.instances.length,
      0,
    );
  },
});
</script>
