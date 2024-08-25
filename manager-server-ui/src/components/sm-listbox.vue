<script setup>
import {
  Listbox,
  ListboxButton,
  ListboxOption,
  ListboxOptions,
} from '@headlessui/vue';
import { ref, watch } from 'vue';

const props = defineProps({
  dataList: {
    type: Array,
    required: true,
  },
  modelValue: {
    type: [String, Number],
    required: true,
  },
  valueField: {
    type: String,
    default: null,
  },
  labelField: {
    type: String,
    default: null,
  },
});

const localModelValue = ref(props.modelValue);

watch(
  () => props.modelValue,
  (newModelValue) => {
    localModelValue.value = newModelValue;
  },
);

const getValue = (item) => {
  if (props.valueField) {
    return item[props.valueField];
  } else {
    return item;
  }
};

const getLabel = (item) => {
  if (props.labelField) {
    return item[props.labelField];
  } else {
    return item;
  }
};
</script>

<template>
  <Listbox v-model="localModelValue">
    <div class="relative mt-1">
      <ListboxButton
        class="relative w-full cursor-default rounded bg-white py-2 pl-3 pr-10 text-left shadow-md focus:outline-none focus-visible:border-indigo-500 focus-visible:ring-2 focus-visible:ring-white/75 focus-visible:ring-offset-2 focus-visible:ring-offset-orange-300 sm:text-sm"
      >
        <span class="block truncate">{{ localModelValue }}</span>
        <span
          class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2"
        >
          <font-awesome-icon
            class="h-5 w-5 text-gray-400"
            :icon="['fas', 'angle-down']"
            aria-hidden="true"
          />
          <font-awesome-icon
            class="h-5 w-5 text-gray-400"
            :icon="['fas', 'angle-up']"
            aria-hidden="true"
          />
        </span>
      </ListboxButton>
      <transition
        leave-active-class="transition duration-100 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <ListboxOptions
          class="absolute mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black/5 focus:outline-none sm:text-sm"
        >
          <ListboxOption
            v-for="item in dataList"
            v-slot="{ active, selected }"
            :key="getLabel(item)"
            :value="getValue(item)"
            as="template"
          >
            <li
              :class="[
                active ? 'bg-amber-100 text-amber-900' : 'text-gray-900',
                'relative cursor-default select-none py-2 pl-10 pr-4',
              ]"
            >
              <span
                :class="[
                  selected ? 'font-medium' : 'font-normal',
                  'block truncate',
                ]"
                >{{ getLabel(item) }}</span
              >
              <span
                v-if="selected"
                class="absolute inset-y-0 left-0 flex items-center pl-3 text-amber-600"
              >
                <font-awesome-icon
                  class="h-5 w-5"
                  :icon="['fas', 'check']"
                  aria-hidden="true"
                />
              </span>
            </li>
          </ListboxOption>
        </ListboxOptions>
      </transition>
    </div>
  </Listbox>
</template>
