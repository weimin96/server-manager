

<template>
  <sba-panel :title="$t('instances.details.metadata.title')" :seamless="true">
    <sba-key-value-table v-if="!isEmptyMetadata" :map="metadata" />
  </sba-panel>
</template>

<script>
import Instance from '@/services/instance';

export default {
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  computed: {
    metadata() {
      return this.instance.registration.metadata;
    },
    isEmptyMetadata() {
      return Object.keys(this.instance.registration.metadata).length <= 0;
    },
  },
};
</script>
