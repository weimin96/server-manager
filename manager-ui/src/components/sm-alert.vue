<template>
  <div
    v-if="hasError"
    :class="classNames(alertClass, borderClassNames)"
    class="rounded-b px-4 py-3 shadow-sm backdrop-filter backdrop-blur-xs bg-opacity-80 my-3"
    role="alert"
  >
    <div class="flex">
      <div class="py-1">
        <font-awesome-icon :icon="icon" class="mr-4" prefix="fa" size="1x" />
      </div>
      <div class="grid grid-cols-1 place-content-center">
        <p v-if="title" class="font-bold" v-text="title" />
        <p class="text-sm" v-html="message" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { faCheckCircle } from '@fortawesome/free-solid-svg-icons/faCheckCircle';
import { faExclamationCircle } from '@fortawesome/free-solid-svg-icons/faExclamationCircle';
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons/faInfoCircle';
import classNames from 'classnames';
import { defineComponent } from 'vue';

import FontAwesomeIcon from '@/components/font-awesome-icon';

export const Severity = {
  ERROR: 'ERROR',
  WARN: 'WARN',
  INFO: 'INFO',
  SUCCESS: 'SUCCESS',
};

export default defineComponent({
  name: 'SmAlert',
  components: { FontAwesomeIcon },
  props: {
    title: {
      type: String,
      default: undefined,
    },
    error: {
      type: [Error, String, Event],
      default: null,
    },
    severity: {
      type: String,
      default: Severity.ERROR,
    },
  },
  data() {
    return {
      classNames,
      alertClass: {
        'bg-red-100 border-red-400 text-red-700':
          this.severity.toUpperCase() === Severity.ERROR,
        'bg-orange-100 border-orange-500 text-orange-700':
          this.severity.toUpperCase() === Severity.WARN,
        'bg-blue-100 border-blue-500 text-blue-900':
          this.severity.toUpperCase() === Severity.INFO,
        'bg-teal-100 border-teal-500 text-teal-900':
          this.severity.toUpperCase() === Severity.SUCCESS,
      },
      textColor: {
        'text-red-700': this.severity.toUpperCase() === Severity.ERROR,
        'text-orange-700': this.severity.toUpperCase() === Severity.WARN,
        'text-blue-900': this.severity.toUpperCase() === Severity.INFO,
        'text-teal-900': this.severity.toUpperCase() === Severity.SUCCESS,
      },
    };
  },
  computed: {
    message() {
      if (this.error instanceof Error) {
        return this.error.message;
      }
      if (typeof this.error === 'string') {
        return this.error;
      }

      return null;
    },
    borderClassNames() {
      if (this.$attrs.class?.indexOf('border-') >= 0) {
        return [];
      } else {
        return ['border-t-4'];
      }
    },
    hasError() {
      return this.error !== undefined && this.error !== null;
    },
    icon() {
      switch (this.severity.toUpperCase()) {
        case Severity.ERROR:
        case Severity.WARN:
          return faExclamationCircle;
        case Severity.SUCCESS:
          return faCheckCircle;
        case Severity.INFO:
        default:
          return faInfoCircle;
      }
    },
  },
});
</script>
