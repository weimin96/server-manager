<template>
  <sm-panel title="'元数据'" :seamless="true">
    <sm-key-value-table v-if="!isEmptyMetadata" :map="metadata" />
  </sm-panel>
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
