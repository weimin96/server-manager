<template>
  <div class="route-container">
    <div class="route">
      <div class="route-header">
        <font-awesome-icon icon="search" />&nbsp;
        <span v-text="$t('instances.gateway.route.predicates')" />
      </div>
      <div class="route-content" v-text="route.predicate" />
    </div>

    <font-awesome-icon icon="angle-double-right" class="route-spacer" />

    <div v-if="route.filters.length > 0" class="route">
      <div class="route-header">
        <font-awesome-icon icon="filter" />&nbsp;
        <span v-text="$t('instances.gateway.route.filters')" />
      </div>
      <div
        v-for="filter in route.filters"
        :key="filter"
        class="route-content"
        v-text="filter"
      />
    </div>

    <font-awesome-icon
      v-if="route.filters.length > 0"
      icon="angle-double-right"
      class="route-spacer"
    />

    <div class="route">
      <div class="route-header">
        <font-awesome-icon icon="map-marker-alt" />&nbsp;
        <span v-text="$t('instances.gateway.route.uri')" />
      </div>
      <div class="route-content" v-text="route.uri" />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    route: {
      type: Object,
      required: true,
    },
  },
  methods: {
    transformArgs(args) {
      return Object.entries(args).map(([key, value]) => `${key} : ${value}`);
    },
  },
};
</script>

<style lang="css">
.route {
  display: block;
  min-width: 12em;
  max-width: 28em;
  padding: 0.5em;
  margin: 1.25em;
  background-color: #fff;
  border-radius: 6px;
  box-shadow:
    0 2px 3px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(0, 0, 0, 0.1);
}
.route-container {
  display: flex;
  align-items: center;
}
.route-spacer {
  font-size: 1.25rem;
  min-width: 1em;
  max-width: 3.5em;
}
.route-header {
  font-size: 1.25rem;
}
.route-content {
  background-color: #00d1b2;
  color: #fff;
  border-radius: 4px;
  padding: 0.25em;
  margin: 0.25em;
}
.route-category {
  font-weight: 700;
  border-bottom: 1px solid #fafafa;
  display: block;
}
</style>
