

<template>
  <section class="section">
    <global-filters :instance="instance" />
    <routes :instance="instance" />
  </section>
</template>

<script>
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import globalFilters from '@/views/instances/gateway/global-filters';
import routes from '@/views/instances/gateway/routes';

export default {
  components: {
    globalFilters,
    routes,
  },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/gateway',
      parent: 'instances',
      path: 'gateway',
      component: this,
      label: 'instances.gateway.label',
      group: VIEW_GROUP.WEB,
      order: 960,
      isEnabled: ({ instance }) => instance.hasEndpoint('gateway'),
    });
  },
};
</script>
