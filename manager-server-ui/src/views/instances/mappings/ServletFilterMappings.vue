<template>
  <div class="table-container">
    <table v-if="servletFilters.length" class="table table-full">
      <colgroup>
        <col class="w-40" />
        <col class="w-40" />
      </colgroup>
      <thead>
        <tr>
          <th v-text="$t('instances.mappings.url_pattern')" />
          <th v-text="$t('instances.mappings.servlet_name')" />
          <th v-text="$t('instances.mappings.filter_name')" />
          <th v-text="$t('instances.mappings.class')" />
        </tr>
      </thead>
      <tbody>
        <template
          v-for="servletFilterMapping in servletFilters"
          :key="`${servletFilterMapping.name}`"
        >
          <tr>
            <td>
              <div
                v-for="mapping in servletFilterMapping.urlPatternMappings"
                :key="`${servletFilterMapping.name}_${mapping}`"
              >
                <code v-text="mapping" />
              </div>
            </td>
            <td>
              <div
                v-for="mapping in servletFilterMapping.servletNameMappings"
                :key="`${servletFilterMapping.name}_${mapping}`"
                class="is-breakable"
                v-text="mapping"
              />
            </td>
            <td class="is-breakable" v-text="servletFilterMapping.name" />
            <td class="is-breakable" v-text="servletFilterMapping.className" />
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>
<script>
export default {
  props: {
    servletFilters: {
      type: Array,
      default: () => [],
    },
  },
};
</script>
