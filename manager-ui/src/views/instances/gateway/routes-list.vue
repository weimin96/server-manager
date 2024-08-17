<template>
  <table class="table routes is-fullwidth is-hoverable">
    <thead>
      <tr>
        <th v-text="$t('instances.gateway.route.id')" />
        <th v-text="$t('instances.gateway.route.order')" />
        <th />
      </tr>
    </thead>
    <tbody>
      <template v-for="route in routes" :key="route.route_id">
        <tr
          class="is-selectable"
          @click="
            showDetails[route.route_id]
              ? delete showDetails[route.route_id]
              : (showDetails[route.route_id] = true)
          "
        >
          <td class="is-breakable" v-text="route.route_id" />
          <td v-text="route.order" />
          <td class="routes__delete-action">
            <sm-confirm-button
              :class="{
                'is-loading': deleting[route.route_id] === 'executing',
                'is-danger': deleting[route.route_id] === 'failed',
                'is-info': deleting[route.route_id] === 'completed',
              }"
              :disabled="deleting[route.route_id] === 'executing'"
              class="button refresh-button is-light"
              @click.stop="deleteRoute(route.route_id)"
            >
              <span
                v-if="deleting[route.route_id] === 'completed'"
                v-text="$t('instances.gateway.route.deleted')"
              />
              <span
                v-else-if="deleting[route.route_id] === 'failed'"
                v-text="$t('instances.gateway.route.delete_failed')"
              />
              <span v-else>
                <font-awesome-icon icon="trash" />
                <span v-text="$t('instances.gateway.route.delete')" />
              </span>
            </sm-confirm-button>
          </td>
        </tr>
        <tr
          v-if="showDetails[route.route_id]"
          :key="`${route.route_id}-detail`"
        >
          <td class="has-background-white-bis" colspan="3">
            <route-definition
              v-if="route.route_definition"
              :route-definition="route.route_definition"
            />
            <route v-if="route.uri" :route="route" />
            <pre
              v-else-if="route.route_object"
              class="is-breakable"
              v-text="toJson(route.route_object)"
            />
            <span
              v-else
              class="is-muted"
              v-text="$t('instances.gateway.route.no_definition_provided')"
            />
          </td>
        </tr>
      </template>
      <tr v-if="routes.length === 0">
        <td class="is-muted" colspan="3">
          <p
            v-if="isLoading"
            class="is-loading"
            v-text="$t('instances.gateway.route.loading')"
          />
          <p v-else v-text="$t('instances.gateway.route.no_routes_found')" />
        </td>
      </tr>
    </tbody>
  </table>
</template>
<script>
import Instance from '@/services/instance';
import { from, listen } from '@/utils/rxjs';
import Route from '@/views/instances/gateway/route';
import RouteDefinition from '@/views/instances/gateway/route-definition';

export default {
  components: { RouteDefinition, Route },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    routes: {
      type: Array,
      required: true,
    },
    isLoading: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['route-deleted'],
  data: () => ({
    showDetails: {},
    deleting: {},
  }),
  methods: {
    toJson(obj) {
      return JSON.stringify(obj, null, 4);
    },
    deleteRoute(routeId) {
      from(this.instance.deleteGatewayRoute(routeId))
        .pipe(listen((status) => (this.deleting[routeId] = status)))
        .subscribe({
          complete: () => this.$emit('route-deleted'),
        });
    },
  },
};
</script>
<style lang="css">
.routes td,
.routes th {
  vertical-align: middle;
}

.routes__delete-action {
  text-align: right;
}
</style>
