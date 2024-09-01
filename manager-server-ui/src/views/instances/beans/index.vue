<template>
  <sm-instance-section :loading="isLoading">
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
          <template #append>
            {{ filterResultString }}
          </template>
        </sm-input>
      </sm-sticky-subnav>
    </template>

    <template v-for="context in filteredContexts" :key="context.name">
      <sm-panel :header-sticks-below="'#subnavigation'" :title="context.name">
        <beans-list :key="`${context.name}-beans`" :beans="context.beans" />
      </sm-panel>
    </template>
  </sm-instance-section>
</template>

<script>
import { isEmpty } from 'lodash-es';

import Instance from '@/services/instance';
import { compareBy } from '@/utils/collections';
import shortenClassname from '@/utils/shortenClassname';
import { VIEW_GROUP } from '@/views/ViewGroup';
import BeansList from '@/views/instances/beans/beans-list';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

class Bean {
  constructor(name, bean) {
    Object.assign(this, bean);
    this.name = name;
    this.shortName = shortenClassname(this.name, 80);
    this.shortType = shortenClassname(this.type, 80);
  }
}

const flattenBeans = (beans) => {
  return Object.keys(beans).map((key) => {
    return new Bean(key, beans[key]);
  });
};

const flattenContexts = (beanData) => {
  if (isEmpty(beanData.contexts)) {
    return [];
  }
  return Object.keys(beanData.contexts).map((key) => ({
    beans: flattenBeans(beanData.contexts[key].beans),
    name: key,
    parent: beanData.contexts[key].parentId,
  }));
};

export default {
  components: { SmInstanceSection, BeansList },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    isLoading: false,
    error: null,
    contexts: [],
    filter: '',
  }),
  computed: {
    filterResultString() {
      const totalBeans = this.contexts.reduce((count, ctx) => {
        return count + ctx.beans?.length;
      }, 0);
      const filteredBeansLength = this.filteredContexts.reduce((count, ctx) => {
        return count + ctx.beans?.length;
      }, 0);

      return `${filteredBeansLength}/${totalBeans}`;
    },
    filteredContexts() {
      const filterFn = this.getFilterFn();
      return this.contexts.map((ctx) => ({
        ...ctx,
        beans: ctx.beans.filter(filterFn).sort(compareBy((bean) => bean.name)),
      }));
    },
  },
  created() {
    this.fetchBeans();
  },
  methods: {
    getFilterFn() {
      if (!this.filter) {
        return () => true;
      }
      const regex = new RegExp(this.filter, 'i');
      return (bean) =>
        bean.name.match(regex) ||
        (bean.aliases && bean.aliases.some((alias) => alias.match(regex)));
    },
    async fetchBeans() {
      this.error = null;
      this.isLoading = true;
      try {
        const res = await this.instance.fetchBeans();
        this.contexts = flattenContexts(res.data);
      } catch (error) {
        console.warn('Fetching beans failed:', error);
        this.error = error;
      }
      this.isLoading = false;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/beans',
      parent: 'instances',
      path: 'beans',
      label: 'instances.beans.label',
      group: VIEW_GROUP.INSIGHTS,
      component: this,
      order: 110,
      isEnabled: ({ instance }) => instance.hasEndpoint('beans'),
    });
  },
};
</script>
