<template>
  <div :class="{ 'is-loading': isLoading }">
    <sm-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />

    <sm-panel :header-sticks-below="'#subnavigation'" title="Global Filters">
      <div v-if="globalFilters.length > 0" class="field">
        <p class="control is-expanded has-icons-left">
          <input v-model="filterCriteria" class="input" type="search" />
          <span class="icon is-small is-left">
            <font-awesome-icon icon="filter" />
          </span>
        </p>
      </div>

      <table class="table is-fullwidth">
        <thead>
          <tr>
            <th v-text="$t('instances.gateway.filters.filter_name')" />
            <th v-text="$t('instances.gateway.filters.order')" />
          </tr>
        </thead>
        <tbody>
          <tr v-for="filter in globalFilters" :key="filter.name">
            <td>
              <span class="is-breakable" v-text="filter.name" />
              <span class="is-muted" v-text="`@${filter.objectId}`" />
            </td>
            <td v-text="filter.order" />
          </tr>
          <tr v-if="globalFilters.length === 0">
            <td class="is-muted" colspan="7 ">
              <p
                v-if="isLoading"
                class="is-loading"
                v-text="$t('instances.gateway.filters.loading')"
              />
              <p
                v-else
                v-text="$t('instances.gateway.filters.no_filters_found')"
              />
            </td>
          </tr>
        </tbody>
      </table>
    </sm-panel>
  </div>
</template>

<script>
import Instance from '@/services/instance';
import { compareBy } from '@/utils/collections';

const globalFilterHasKeyword = (globalFilter, keyword) => {
  return globalFilter.name.toString().toLowerCase().includes(keyword);
};

const sortGlobalFilter = (globalFilters) => {
  return [...globalFilters].sort(compareBy((f) => f.order));
};

export default {
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    isLoading: false,
    error: null,
    $globalFilters: [],
    filterCriteria: null,
  }),
  computed: {
    globalFilters() {
      if (!this.filterCriteria) {
        return sortGlobalFilter(this.$data.$globalFilters);
      }
      const filtered = this.$data.$globalFilters.filter((globalFilter) =>
        globalFilterHasKeyword(globalFilter, this.filterCriteria.toLowerCase()),
      );
      return sortGlobalFilter(filtered);
    },
  },
  created() {
    this.fetchGlobalFilters();
  },
  methods: {
    async fetchGlobalFilters() {
      this.error = null;
      this.isLoading = true;
      try {
        const response = await this.instance.fetchGatewayGlobalFilters();
        this.$data.$globalFilters = Object.entries(response.data).map(
          ([name, order]) => {
            const [className, objectId] = name.split('@');
            return {
              name: className,
              objectId,
              order,
            };
          },
        );
      } catch (error) {
        console.warn('Fetching global filters failed:', error);
        this.error = error;
      }
      this.isLoading = false;
    },
  },
};
</script>
