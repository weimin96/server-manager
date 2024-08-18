<template>
  <MenuItem
    v-slot="{ close }"
    :active="active"
    :as="as"
    :disabled="disabled"
    class="block w-full"
    type="submit"
    v-bind="$attrs"
  >
    <a
      v-if="href"
      :aria-disabled="disabled"
      :href="href"
      class="sm-dropdown-item"
      rel="noopener noreferrer"
      target="_blank"
      @click="(event) => onClick(event, close)"
    >
      <slot :active="active" />
    </a>

    <router-link
      v-else-if="to"
      :aria-disabled="disabled"
      :to="to"
      class="sm-dropdown-item"
      @click="(event) => onClick(event, close)"
    >
      <slot :active="active" />
    </router-link>

    <div
      v-else
      class="sm-dropdown-item"
      @click="(event) => onClick(event, close)"
    >
      <slot />
    </div>
  </MenuItem>
</template>

<script setup>
import { MenuItem } from '@headlessui/vue';

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false,
  },
  active: {
    type: Boolean,
    default: false,
  },
  as: {
    type: String,
    default: 'div',
  },
  href: {
    type: String,
    default: null,
  },
  to: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['click']);

const onClick = ($event, close) => {
  if (!props.href && !props.to) {
    emit('click', $event);
  }

  close();
};
</script>

<style scoped>
.sm-dropdown-item {
  @apply flex w-full items-center rounded-md px-2 py-2 hover:bg-sm-700 hover:text-white;
}

.sm-dropdown-item[aria-disabled='true'] {
  @apply text-gray-400 hover:text-gray-400 hover:bg-transparent cursor-not-allowed;
}

.sm-dropdown-item[active='true'] {
  @apply bg-sm-700 text-white;
}
</style>
