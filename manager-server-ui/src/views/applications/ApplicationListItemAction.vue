<template>
  <sm-button-group class="text-right mr-6">
    <router-link v-slot="{ navigate }" :to="journalLink" custom>
      <button
        class="inline-flex items-center justify-center whitespace-nowrap font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 border border-input bg-background shadow-sm hover:bg-accent hover:text-accent-foreground rounded-md px-3 text-xs h-8 border-dashed"
        type="button"
        @click.stop="navigate"
      >
        <font-awesome-icon icon="history" class="mr-1" />
        日志
      </button>
    </router-link>
  </sm-button-group>
</template>

<script lang="ts" setup>
import { RouteLocationNamedRaw } from 'vue-router';

import Application from '@/services/application';
import Instance from '@/services/instance';

const props = defineProps({
  item: {
    type: [Application, Instance],
    required: true,
  },
});

defineEmits(['filter-settings']);

let journalLink: RouteLocationNamedRaw;
if (props.item instanceof Application) {
  journalLink = {
    name: 'journal',
    query: { application: props.item.name },
  };
} else if (props.item instanceof Instance) {
  journalLink = { name: 'journal', query: { instanceId: props.item.id } };
}
</script>
