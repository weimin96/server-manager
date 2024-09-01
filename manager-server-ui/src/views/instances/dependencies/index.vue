<template>
  <sm-instance-section :loading="!hasLoaded">
    <template #before>
      <sm-sticky-subnav>
        <sm-input
          v-model="filter"
          name="filter"
          type="search"
        >
          <template #prepend>
            <font-awesome-icon icon="filter" />
          </template>
        </sm-input>
      </sm-sticky-subnav>
    </template>
    <template v-for="sbomId in sboms" :key="sbomId">
      <sbom-list :instance="instance" :sbom-id="sbomId" :filter="filter" />
    </template>
  </sm-instance-section>
</template>
<script>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

import SmStickySubnav from '@/components/sm-sticky-subnav';

import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SbomList from '@/views/instances/dependencies/SbomList';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

export default {
  components: {
    FontAwesomeIcon,
    SmStickySubnav,
    SbomList,
    SmInstanceSection,
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
    sboms: [],
    filter: '',
  }),
  created() {
    this.fetchSbomIds();
  },
  methods: {
    async fetchSbomIds() {
      this.error = null;
      try {
        const res = await this.instance.fetchSbomIds();
        this.sboms = res.data.ids;
      } catch (error) {
        console.warn('Fetching sbom ids failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/dependencies',
      parent: 'instances',
      path: 'dependencies',
      label: 'instances.dependencies.label',
      group: VIEW_GROUP.DEPENDENCIES,
      order: 1,
      component: this,
      isEnabled: ({ instance }) => instance.hasEndpoint('sbom'),
    });
  },
};
</script>
