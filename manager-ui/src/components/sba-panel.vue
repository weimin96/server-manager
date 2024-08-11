<template>
  <div
    :id="$attrs.id"
    class="shadow-sm border rounded break-inside-avoid mb-6"
    :aria-expanded="$attrs.ariaExpanded"
  >
    <header
      v-if="hasTitle"
      ref="header"
      v-sticks-below="headerSticksBelow"
      class="rounded-t flex justify-between px-4 pt-5 pb-5 border-b sm:px-6 items-center bg-white transition-all"
    >
      <h3 class="text-lg leading-6 font-medium text-gray-900">
        <button class="flex items-center" @click="$emit('title-click')">
          <slot v-if="'prefix' in $slots" name="prefix" />
          <span v-text="title" />
          <span
            v-if="subtitle"
            class="ml-2 text-sm text-gray-500 self-end"
            v-text="subtitle"
          />
          <slot v-if="'title' in $slots" name="title" />
        </button>
      </h3>

      <div>
        <slot v-if="'version' in $slots" name="version" />
      </div>

      <div>
        <slot v-if="'actions' in $slots" name="actions" />
        <sba-icon-button
          v-if="closeable"
          :icon="['far', 'times-circle']"
          @click.stop="$emit('close', $event)"
        />
      </div>
    </header>
    <div
      v-if="'default' in $slots"
      :class="{
        'rounded-t': !hasTitle,
        'rounded-b': !('footer' in $slots),
      }"
      class="border-gray-200 px-4 py-3 bg-white"
    >
      <div :class="{ '-mx-4 -my-3': seamless }">
        <sba-loading-spinner v-if="loading" class="" size="sm" />
        <slot v-if="!loading" />
      </div>
    </div>
    <footer v-if="'footer' in $slots">
      <div class="px-4 py-3 border-t bg-gray-50">
        <slot name="footer" />
      </div>
    </footer>
  </div>
</template>

<script>
import { throttle } from 'lodash-es';

import SbaIconButton from '@/components/sba-icon-button';
import SbaLoadingSpinner from '@/components/sba-loading-spinner';

import sticksBelow from '@/directives/sticks-below';

export default {
  components: { SbaLoadingSpinner, SbaIconButton },
  directives: { sticksBelow },
  props: {
    title: {
      type: String,
      default: undefined,
    },
    subtitle: {
      type: String,
      default: undefined,
    },
    closeable: {
      type: Boolean,
      default: false,
    },
    headerSticksBelow: {
      type: String,
      default: undefined,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    seamless: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['close', 'title-click'],
  data() {
    return {
      headerTopValue: 0,
      onScrollFn: undefined,
    };
  },
  computed: {
    hasTitle() {
      return !!this.title || 'title' in this.$slots || 'actions' in this.$slots;
    },
  },
  mounted() {
    if (this.headerSticksBelow) {
      const header = this.$refs.header;
      this.headerTopValue = +header.style.top.substr(
        0,
        header.style.top.length - 2,
      );

      this.onScrollFn = throttle(this.onScroll, 150);
      document.addEventListener('scroll', this.onScrollFn);
    }
  },
  beforeUnmount() {
    if (this.headerSticksBelow) {
      document.removeEventListener('scroll', this.onScrollFn);
    }
  },
  methods: {
    onScroll() {
      const header = this.$refs.header;
      const boundingClientRect = header.getBoundingClientRect();
      if (boundingClientRect.top <= this.headerTopValue) {
        header.classList.add('!rounded-none', '!py-2');
      } else {
        header.classList.remove('!rounded-none', '!py-2');
      }
    },
  },
};
</script>
