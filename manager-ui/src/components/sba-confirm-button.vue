<template>
  <sba-button
    v-on-clickaway="abort"
    :class="{ 'is-success': confirm }"
    @click="click"
  >
    <slot v-if="confirm" name="confirm">
      {{ $t('term.confirm') }}
    </slot>
    <slot v-else />
  </sba-button>
</template>

<script>
import { directive as onClickaway } from 'vue3-click-away';

export default {
  directives: { onClickaway },
  emits: ['click'],
  data() {
    return {
      confirm: false,
    };
  },
  methods: {
    abort() {
      this.confirm = false;
    },
    click(event) {
      if (this.confirm) {
        this.$emit('click', event);
      } else {
        event.stopPropagation();
      }
      this.confirm = !this.confirm;
    },
  },
};
</script>
