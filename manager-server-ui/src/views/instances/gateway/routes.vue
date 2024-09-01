<template>
  <div :class="{ 'is-loading': isLoading }">
    <sm-panel
      v-if="routes"
      :header-sticks-below="'#subnavigation'"
      title="Routes"
    >
      <refresh-route-cache
        :instance="instance"
        @routes-refreshed="fetchRoutes"
      />

      <div class="field">
        <p class="control is-expanded has-icons-left">
          <input v-model="routesFilterCriteria" class="input" type="search" />
          <span class="icon is-small is-left">
            <font-awesome-icon icon="filter" />
          </span>
        </p>
      </div>

      <routes-list
        :instance="instance"
        :is-loading="isLoading"
        :routes="routes"
        @route-deleted="fetchRoutes"
      />
    </sm-panel>
    <sm-panel title="Add Route">
      <add-route :instance="instance" @route-added="fetchRoutes" />
    </sm-panel>
  </div>
</template>

<script>
import Instance from '@/services/instance';
import { anyValueMatches, compareBy } from '@/utils/collections';
import addRoute from '@/views/instances/gateway/add-route';
import refreshRouteCache from '@/views/instances/gateway/refresh-route-cache';
import routesList from '@/views/instances/gateway/routes-list';

const routeDefinitionMatches = (routeDef, keyword) => {
  if (!routeDef) {
    return false;
  }
  const predicate = (value) => String(value).toLowerCase().includes(keyword);
  return (
    (routeDef.uri && anyValueMatches(routeDef.uri.toString(), predicate)) ||
    anyValueMatches(routeDef.predicates, predicate) ||
    anyValueMatches(routeDef.filters, predicate)
  );
};

const routeMatches = (route, keyword) => {
  return (
    route.route_id.toString().toLowerCase().includes(keyword) ||
    routeDefinitionMatches(route.route_definition, keyword)
  );
};

const sortRoutes = (routes) => {
  return [...routes].sort(compareBy((r) => r.order));
};

export default {
  components: {
    refreshRouteCache,
    routesList,
    addRoute,
  },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    isLoading: false,
    $routes: [],
    routesFilterCriteria: null,
  }),
  computed: {
    routes() {
      if (!this.routesFilterCriteria) {
        return sortRoutes(this.$data.$routes);
      }
      const filtered = this.$data.$routes.filter((route) =>
        routeMatches(route, this.routesFilterCriteria.toLowerCase()),
      );
      return sortRoutes(filtered);
    },
  },
  created() {
    this.fetchRoutes();
  },
  methods: {
    async fetchRoutes() {
      this.isLoading = true;
      try {
        const response = await this.instance.fetchGatewayRoutes();
        this.$data.$routes = response.data;
      } catch (error) {
        console.warn('Fetching routes failed:', error);
        ElMessage.error('加载失败');
      }
      this.isLoading = false;
    },
  },
};
</script>
