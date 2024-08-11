<template>
  <div :class="classNames('inline-flex gap-1', { 'flex-wrap': wrap })">
    <template v-for="(value, key) in tags" :key="key">
      <sba-tag :label="key" :small="small" :value="value" />
    </template>
  </div>
</template>

<script>
import classNames from 'classnames';

export default {
  props: {
    tags: {
      type: Object,
      required: true,
    },
    small: {
      type: Boolean,
      default: false,
    },
    wrap: {
      type: Boolean,
      default: true,
    },
  },
  methods: {
    classNames: classNames,
  },
};
</script>
