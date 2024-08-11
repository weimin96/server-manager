

<template>
  <div class="inline-flex">
    <div class="btn-group">
      <sba-button
        v-for="levelOption in levelOptions"
        :key="levelOption"
        :class="cssClass(levelOption)"
        @click.stop="selectLevel(levelOption)"
        v-text="levelOption"
      />
    </div>
    <sba-button
      class="ml-3"
      :class="{ 'is-loading': getStatusForLevel(null) === 'executing' }"
      :disabled="!isConfigured || !allowReset"
      @click.stop="selectLevel(null)"
      v-text="$t('instances.loggers.reset')"
    />
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: Array,
      required: true,
    },
    levelOptions: {
      type: Array,
      required: true,
    },
    allowReset: {
      type: Boolean,
      default: true,
    },
    status: {
      type: Object,
      default: null,
    },
  },
  emits: ['input'],
  computed: {
    isConfigured() {
      return this.value.some((l) => Boolean(l.configuredLevel));
    },
  },
  methods: {
    hasEffectiveLevel(level) {
      return this.value.some((l) => l.effectiveLevel === level);
    },
    hasConfiguredLevel(level) {
      return this.value.some((l) => l.configuredLevel === level);
    },
    selectLevel(level) {
      this.$emit('input', level);
    },
    getStatusForLevel(level) {
      if (this.status && this.status.level === level) {
        return this.status.status;
      }
    },
    cssClass(level) {
      return {
        'logger-control__level--inherited': !this.hasConfiguredLevel(level),
        'is-active is-danger':
          level === 'TRACE' && this.hasEffectiveLevel('TRACE'),
        'is-active is-warning':
          level === 'DEBUG' && this.hasEffectiveLevel('DEBUG'),
        'is-active is-info': level === 'INFO' && this.hasEffectiveLevel('INFO'),
        'is-active is-success':
          level === 'WARN' && this.hasEffectiveLevel('WARN'),
        'is-active is-light':
          level === 'ERROR' && this.hasEffectiveLevel('ERROR'),
        'is-active is-black': level === 'OFF' && this.hasEffectiveLevel('OFF'),
        'is-loading': this.getStatusForLevel(level) === 'executing',
      };
    },
  },
};
</script>

<style>
.logger-control__level--inherited {
  @apply bg-opacity-70 !important;
}
</style>
