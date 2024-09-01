<template>
  <sm-instance-section :loading="!hasLoaded">
    <template v-for="(context, ctxName) in contexts" :key="ctxName">
      <sm-panel :seamless="true">
        <dispatcher-mappings
          v-if="hasDispatcherServlets(context)"
          :key="`${ctxName}_dispatcherServlets`"
          :dispatchers="context.mappings.dispatcherServlets"
        />

        <dispatcher-mappings
          v-if="hasDispatcherHandlers(context)"
          :key="`${ctxName}_dispatcherHandlers`"
          :dispatchers="context.mappings.dispatcherHandlers"
        />

        <servlet-mappings
          v-if="hasServlet(context)"
          :key="`${ctxName}_servlets`"
          :servlets="context.mappings.servlets"
        />

        <servlet-filter-mappings
          v-if="hasServletFilters(context)"
          :key="`${ctxName}_servletFilters`"
          :servlet-filters="context.mappings.servletFilters"
        />
      </sm-panel>
    </template>
  </sm-instance-section>
</template>

<script>
import SmPanel from '@/components/sm-panel';

import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import DispatcherMappings from '@/views/instances/mappings/DispatcherMappings';
import ServletFilterMappings from '@/views/instances/mappings/ServletFilterMappings';
import ServletMappings from '@/views/instances/mappings/ServletMappings';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

export default {
  components: {
    SmPanel,
    SmInstanceSection,
    DispatcherMappings,
    ServletMappings,
    ServletFilterMappings,
  },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    contexts: null,
  }),
  created() {
    this.fetchMappings();
  },
  methods: {
    hasDispatcherServlets(context) {
      return context?.mappings?.dispatcherServlets !== undefined;
    },
    hasDispatcherHandlers(context) {
      return context?.mappings?.dispatcherHandlers !== undefined;
    },
    hasServlet(context) {
      return context?.mappings?.servlets !== undefined;
    },
    hasServletFilters(context) {
      return context?.mappings?.servletFilters !== undefined;
    },
    async fetchMappings() {
      this.error = null;
      try {
        const res = await this.instance.fetchMappings();
        if (
          res.headers['content-type'].includes(
            'application/vnd.spring-boot.actuator.v2',
          )
        ) {
          this.contexts = res.data.contexts;
        }
      } catch (error) {
        console.warn('Fetching mappings failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/mappings',
      parent: 'instances',
      path: 'mappings',
      label: 'instances.mappings.label',
      group: VIEW_GROUP.WEB,
      component: this,
      order: 450,
      isEnabled: ({ instance }) => instance.hasEndpoint('mappings'),
    });
  },
};
</script>
