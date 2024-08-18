<template>
  <sm-button
    class="border-none sm:m-1 !bg-transparent"
    :title="title"
    size="xs"
    v-on="$attrs"
  >
    <font-awesome-icon :icon="icon" :size="size" :class="iconClass" />
  </sm-button>
</template>

<script>
import SmButton from '@/components/sm-button';

export default {
  components: { SmButton },
  props: {
    title: {
      type: String,
      required: false,
      default: null,
    },
    icon: {
      type: [String, Array],
      required: true,
    },
    size: {
      type: String,
      default: null,
    },
    iconClass: {
      type: String,
      default: null,
    },
  },
};
</script>

<style></style>
