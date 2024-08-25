<template>
  <div class="table-container">
    <table v-if="servlets.length" class="table table-full">
      <colgroup>
        <col class="w-40" />
        <col class="w-40" />
      </colgroup>
      <thead>
        <tr>
          <th v-text="$t('instances.mappings.url_pattern')" />
          <th v-text="$t('instances.mappings.servlet_name')" />
          <th v-text="$t('instances.mappings.class')" />
        </tr>
      </thead>
      <tbody>
        <template
          v-for="servletMapping in servlets"
          :key="`${servletMapping.name}`"
        >
          <tr>
            <td>
              <div
                v-for="mapping in servletMapping.mappings"
                :key="`${servletMapping.name}_${mapping}`"
              >
                <code v-text="mapping" />
              </div>
            </td>
            <td class="is-breakable" v-text="servletMapping.name" />
            <td class="is-breakable" v-text="servletMapping.className" />
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>
<script>
export default {
  props: {
    servlets: {
      type: Array,
      default: () => [],
    },
  },
};
</script>
