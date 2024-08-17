<template>
  <sm-instance-section :error="error" :loading="isLoading">
    <template #before>
      <sm-sticky-subnav v-if="env">
        <div class="flex">
          <div v-if="instance.hasEndpoint('refresh')" class="mr-1">
            <refresh
              :application="application"
              :instance="instance"
              @refresh="fetchEnv"
            />
          </div>
          <div class="flex-1">
            <sm-input
              v-model="filter"
              :placeholder="$t('term.filter')"
              name="filter"
              type="search"
            >
              <template #prepend>
                <font-awesome-icon icon="filter" />
              </template>
            </sm-input>
          </div>
        </div>
      </sm-sticky-subnav>
    </template>

    <template #default>
      <div v-if="env && env?.activeProfiles.length > 0" class="mb-6 gap-1 flex">
        <span v-for="profile in env.activeProfiles" :key="profile">
          <sm-tag
            :key="profile"
            :label="$t('instances.env.active_profile')"
            :value="profile"
          />
        </span>
      </div>

      <sm-env-manager
        v-if="env && hasEnvManagerSupport"
        :application="application"
        :instance="instance"
        :property-sources="env.propertySources"
        @refresh="fetchEnv"
        @update="fetchEnv"
      />

      <sm-modal data-testid="refreshModal">
        <template #header>
          <span v-text="$t('instances.env.context_refreshed')" />
        </template>
        <template #body>
          <span v-html="$t('instances.env.refreshed_configurations')" />
        </template>
      </sm-modal>

      <sm-panel
        v-for="propertySource in propertySources"
        :key="propertySource.name"
        :header-sticks-below="'#subnavigation'"
        :title="propertySource.name"
      >
        <div
          v-if="
            propertySource.properties &&
            Object.keys(propertySource.properties).length > 0
          "
          class="-mx-4 -my-3"
        >
          <div
            v-for="(value, name) in propertySource.properties"
            :key="`${propertySource.name}-${name}`"
            class="bg-white px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-500">
              <span v-text="name" /><br />
              <small v-if="value.origin" v-text="value.origin" />
            </dt>
            <dd
              class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2"
              v-text="getValue(name, value.value)"
            />
          </div>
        </div>
        <p v-else class="is-muted" v-text="$t('instances.env.no_properties')" />
      </sm-panel>
    </template>
  </sm-instance-section>
</template>

<script>
import { pickBy } from 'lodash-es';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SmEnvManager from '@/views/instances/env/env-manager.vue';
import refresh from '@/views/instances/env/refresh';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

const filterProperty = (needle) => (property, name) => {
  return (
    name.toString().toLowerCase().includes(needle) ||
    (property.value && property.value.toString().toLowerCase().includes(needle))
  );
};
const filterProperties = (needle, properties) =>
  pickBy(properties, filterProperty(needle));
const filterPropertySource = (needle) => (propertySource) => {
  if (!propertySource || !propertySource.properties) {
    return null;
  }
  return {
    ...propertySource,
    properties: filterProperties(needle, propertySource.properties),
  };
};

export default {
  components: { SmInstanceSection, SmEnvManager, refresh },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    application: {
      type: Application,
      required: true,
    },
  },
  data: () => ({
    isLoading: true,
    error: null,
    env: null,
    filter: null,
    hasEnvManagerSupport: false,
    propertyNamesToEscape: ['line.separator'],
  }),
  computed: {
    propertySources() {
      if (!this.env) {
        return [];
      }
      if (!this.filter) {
        return this.env.propertySources;
      }
      return this.env.propertySources
        .map(filterPropertySource(this.filter.toLowerCase()))
        .filter((ps) => ps && Object.keys(ps.properties).length > 0);
    },
  },
  created() {
    this.fetchEnv();
    this.determineEnvManagerSupport();
  },
  methods: {
    async fetchEnv() {
      this.error = null;
      try {
        const res = await this.instance.fetchEnv();
        this.env = res.data;
      } catch (error) {
        console.warn('Fetching environment failed:', error);
        this.error = error;
      }
      this.isLoading = false;
    },
    async determineEnvManagerSupport() {
      try {
        this.hasEnvManagerSupport = await this.instance.hasEnvManagerSupport();
      } catch (error) {
        console.warn('Determine env manager support failed:', error);
        this.hasEnvManagerSupport = false;
      }
    },
    getValue(name, value) {
      if (this.propertyNamesToEscape.includes(name)) {
        return value.replace(/\n/g, '\\n').replace(/\r/g, '\\r');
      }
      return value;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/env',
      parent: 'instances',
      path: 'env',
      component: this,
      label: 'instances.env.label',
      group: VIEW_GROUP.INSIGHTS,
      order: 100,
      isEnabled: ({ instance }) => instance.hasEndpoint('env'),
    });
  },
};
</script>
