<template>
  <sba-button
    class="border-none sm:m-1 !bg-transparent"
    :title="title"
    size="xs"
    v-on="$attrs"
  >
    <font-awesome-icon :icon="icon" :size="size" :class="iconClass" />
  </sba-button>
</template>

<script>
import SbaButton from '@/components/sba-button';

export default {
  components: { SbaButton },
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
