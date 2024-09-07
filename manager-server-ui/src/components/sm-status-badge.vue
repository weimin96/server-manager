<template>
  <span :class="classNames('status-badge', healthStatus)" v-text="status" />
</template>

<script>
import classNames from 'classnames';

import { HealthStatus } from '@/main/HealthStatus';

export default {
  props: {
    status: {
      type: [HealthStatus, String, Number],
      required: true,
    },
  },
  computed: {
    healthStatus() {
      return `${this.status}`.toLowerCase();
    },
  },
  methods: {
    classNames,
  },
};
</script>

<style scoped>
.status-badge {
  @apply bg-gray-200 text-black text-xs inline-flex items-center uppercase  rounded-lg overflow-hidden px-8 py-1.5;
}
.up {
  @apply bg-cyan-300 text-gray-100;
}
.down {
  @apply bg-red-200 text-gray-100;
}
.restricted {
  @apply bg-yellow-400 text-gray-100;
}
</style>
