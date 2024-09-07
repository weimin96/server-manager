<template>
  <div
    :id="$attrs.id"
    class="border border-b-0 rounded break-inside-avoid px-6 pb-1"
    :aria-expanded="$attrs.ariaExpanded"
  >
    <header
      v-if="hasTitle"
      ref="header"
      v-sticks-below="headerSticksBelow"
      class="rounded-t flex justify-between py-5 items-center bg-white transition-all"
    >
      <h3 class="text-lg leading-6 font-medium text-gray-900">
        <button class="flex items-center" @click="$emit('title-click')">
          <slot v-if="'prefix' in $slots" name="prefix" />
          <div class="flex flex-col p-2">
            <span class="font-semibold" v-text="title" />
            <span
              v-if="subtitle"
              class="text-sm text-gray-600 self-start pt-1"
              v-text="subtitle"
            />
          </div>
        </button>
      </h3>
      <div>
        <slot v-if="'actions' in $slots" name="actions" />
        <sm-icon-button
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
        <sm-loading-spinner v-if="loading" class="" size="sm" />
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

import SmIconButton from '@/components/sm-icon-button';
import SmLoadingSpinner from '@/components/sm-loading-spinner';

import sticksBelow from '@/directives/sticks-below';

export default {
  components: { SmLoadingSpinner, SmIconButton },
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
