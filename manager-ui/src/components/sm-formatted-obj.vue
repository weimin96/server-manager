<template>
  <div class="formatted" v-html="formatted" />
</template>

<script>
import { Autolink } from '@/utils/autolink';
import objToString from '@/utils/objToString';

const autolink = new Autolink({
  truncate: {
    length: 50,
    location: 'smart',
  },
});

export default {
  props: {
    value: {
      type: null,
      required: true,
    },
  },
  computed: {
    formatted() {
      return autolink(objToString(this.value));
    },
  },
};
</script>

<style>
.formatted {
  white-space: break-spaces;
  word-break: break-word;
}
</style>
