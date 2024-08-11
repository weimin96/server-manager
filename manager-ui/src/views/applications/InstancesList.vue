<template>
  <ul>
    <li
      v-for="instance in instances"
      :key="instance.id"
      :data-testid="instance.id"
      class="flex items-center hover:bg-gray-100 p-2 pr-6"
      @click.stop="showDetails(instance)"
    >
      <div class="pr-3 md:w-16 text-center">
        <sba-status
          :date="instance.statusTimestamp"
          :status="instance.statusInfo.status"
        />
      </div>
      <div class="flex-auto xl:flex-1 xl:w-1/4 truncate">
        <a
          @click.stop
          v-text="
            instance.registration.serviceUrl || instance.registration.healthUrl
          "
        />
        <sba-tag
          v-if="instance.registration.metadata?.['group']"
          class="ml-2"
          :value="instance.registration.metadata?.['group']"
          small
        />
        <br />
        <span class="text-sm italic" v-text="instance.id" />
      </div>
      <div
        class="hidden xl:block w-1/4"
        :class="{
          'overflow-x-scroll': Object.keys(instance.tags ?? {}).length > 0,
        }"
      >
        <sba-tags :small="true" :tags="instance.tags" :wrap="false" />
      </div>
      <div
        class="hidden xl:block w-2/12 text-center"
        v-text="instance.buildVersion"
      />
      <div class="hidden md:block flex-1 text-right">
        <slot :instance="instance" name="actions" />
      </div>
    </li>
  </ul>
</template>

<script lang="ts" setup>
import { PropType } from 'vue';
import { useRouter } from 'vue-router';

import Instance from '@/services/instance';

const router = useRouter();

defineProps({
  instances: {
    type: Object as PropType<Array<Instance>>,
    default: () => [] as Instance[],
  },
  showNotificationSettings: {
    type: Boolean,
    default: false,
  },
  hasActiveNotificationFilter: {
    type: Function,
    default: () => false,
  },
});

const showDetails = (instance: Instance) => {
  router.push({
    name: 'instances/details',
    params: { instanceId: instance.id },
  });
};
</script>

<style lang="css">
.instances-list td {
  vertical-align: middle;
}
</style>
