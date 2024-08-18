<template>
  <div class="flex gap-1 w-full justify-end">
    <sm-button primary @click="() => close(true)" v-text="labelOk" />
    <sm-button
      class="button"
      @click="() => close(false)"
      v-text="labelCancel"
    />
  </div>
</template>

<script>
import SmButton from '@/components/sm-button';

import eventBus from '@/services/bus';

export default {
  components: { SmButton },
  props: {
    labelCancel: {
      type: String,
      required: true,
    },
    labelOk: {
      type: String,
      required: true,
    },
  },
  methods: {
    close(result) {
      eventBus.emit('sm-modal-close', result);
    },
  },
};
</script>
