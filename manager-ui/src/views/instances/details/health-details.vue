<template>
  <dl
    class="px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
    :class="{ 'bg-white': index % 2 === 0, 'bg-gray-50': index % 2 !== 0 }"
  >
    <dt
      :id="'health-detail__' + name"
      class="text-sm font-medium text-gray-500"
    >
      {{ name }}
    </dt>
    <dd
      class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2"
      :aria-labelledby="'health-detail__' + name"
    >
      <sba-status-badge :status="health.status" />

      <dl v-if="details && details.length > 0" class="grid grid-cols-2 mt-2">
        <template v-for="detail in details" :key="detail.name">
          <dt class="font-medium" v-text="detail.name" />
          <dd
            v-if="name === 'diskSpace'"
            v-text="
              typeof detail.value === 'number'
                ? prettyBytes(detail.value)
                : detail.value
            "
          />
          <dd v-else-if="typeof detail.value === 'object'">
            <pre
              class="break-words whitespace-pre-wrap"
              v-text="toJson(detail.value)"
            />
          </dd>
          <dd
            v-else
            class="break-words whitespace-pre-wrap"
            v-text="detail.value"
          />
        </template>
      </dl>
    </dd>
  </dl>

  <health-details
    v-for="(child, idx) in childHealth"
    :key="child.name"
    :index="idx + 1"
    :name="child.name"
    :health="child.value"
  />
</template>

<script>
import prettyBytes from 'pretty-bytes';

const isChildHealth = (value) => {
  return value !== null && typeof value === 'object' && 'status' in value;
};

export default {
  name: 'HealthDetails',
  props: {
    name: {
      type: String,
      required: true,
    },
    health: {
      type: Object,
      required: true,
    },
    index: {
      type: Number,
      default: 0,
    },
  },
  computed: {
    details() {
      if (this.health.details) {
        return Object.entries(this.health.details)
          .filter(([, value]) => !isChildHealth(value))
          .map(([name, value]) => ({ name, value }));
      }
      return [];
    },
    childHealth() {
      if (this.health.details) {
        return Object.entries(this.health.details)
          .filter(([, value]) => isChildHealth(value))
          .map(([name, value]) => ({ name, value }));
      }
      return [];
    },
  },
  methods: {
    prettyBytes,
    toJson(obj) {
      return JSON.stringify(obj, null, 2);
    },
  },
};
</script>
