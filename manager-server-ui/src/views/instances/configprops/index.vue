<template>
  <sm-instance-section :loading="!hasLoaded">
    <template #before>
      <sm-sticky-subnav>
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
      </sm-sticky-subnav>
    </template>

    <sm-panel
      v-for="bean in configurationPropertiesBeans"
      :key="bean.name"
      :header-sticks-below="'#subnavigation'"
      :title="bean.name"
    >
      <div class="-mx-4 -my-3">
        <table
          v-if="Object.keys(bean.properties).length > 0"
          class="table-auto w-full"
        >
          <tr
            v-for="(value, name, idx) in bean.properties"
            :key="`${bean.name}-${name}`"
            :class="{ 'bg-gray-50': idx % 2 === 0 }"
          >
            <td class="w-1/2 px-4 py-3" v-text="name" />
            <td class="px-4 py-3" v-text="value" />
          </tr>
        </table>
      </div>
    </sm-panel>
  </sm-instance-section>
</template>

<script>
import { isEmpty, mapKeys, pickBy } from 'lodash-es';

import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

const filterProperty = (needle) => (value, name) => {
  return (
    name.toString().toLowerCase().includes(needle) ||
    (value && value.toString().toLowerCase().includes(needle))
  );
};
const filterProperties = (needle, properties) =>
  pickBy(properties, filterProperty(needle));
const filterConfigurationProperties = (needle) => (propertySource) => {
  if (!propertySource || !propertySource.properties) {
    return null;
  }
  return {
    ...propertySource,
    properties: filterProperties(needle, propertySource.properties),
  };
};

function flattenBean(obj, prefix = '') {
  if (Object(obj) !== obj) {
    return { [prefix]: obj };
  }

  if (Array.isArray(obj)) {
    if (obj.length === 0) {
      return { [prefix]: [] };
    } else {
      return obj
        .map((value, idx) => flattenBean(value, `${prefix}[${idx}]`))
        .reduce((c, n) => ({ ...c, ...n }), {});
    }
  } else {
    if (isEmpty(obj)) {
      return { [prefix]: {} };
    } else {
      return Object.entries(obj)
        .map(([name, value]) =>
          flattenBean(value, prefix ? `${prefix}.${name}` : name),
        )
        .reduce((c, n) => ({ ...c, ...n }), {});
    }
  }
}

const flattenConfigurationPropertiesBeans = (configprops) => {
  const propertySources = [];
  const contextNames = Object.keys(configprops.contexts);

  for (const contextName of contextNames) {
    const context = configprops.contexts[contextName];
    const beanNames = Object.keys(context.beans);

    for (const beanName of beanNames) {
      const bean = context.beans[beanName];
      const properties = mapKeys(
        flattenBean(bean.properties),
        (value, key) => `${bean.prefix}.${key}`,
      );
      propertySources.push({
        name:
          contextNames.length > 1 ? `${contextName}: ${beanName}` : beanName,
        properties,
      });
    }
  }

  return propertySources;
};

export default {
  components: { SmInstanceSection },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    configprops: null,
    filter: null,
  }),
  computed: {
    configurationPropertiesBeans() {
      if (!this.configprops) {
        return [];
      }
      const configurationProperties = flattenConfigurationPropertiesBeans(
        this.configprops,
      );
      if (!this.filter) {
        return configurationProperties;
      }
      return configurationProperties
        .map(filterConfigurationProperties(this.filter.toLowerCase()))
        .filter((ps) => ps && Object.keys(ps.properties).length > 0);
    },
  },
  created() {
    this.fetchConfigprops();
  },
  methods: {
    async fetchConfigprops() {
      this.error = null;
      try {
        const configprops = await this.instance.fetchConfigprops();
        this.configprops = configprops.data;
      } catch (error) {
        console.warn('Fetching configuration properties failed:', error);
        this.error = error;
      }
      this.hasLoaded = true;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/configprops',
      parent: 'instances',
      path: 'configprops',
      component: this,
      label: 'instances.configprops.label',
      group: VIEW_GROUP.INSIGHTS,
      order: 110,
      isEnabled: ({ instance }) => instance.hasEndpoint('configprops'),
    });
  },
};
</script>
