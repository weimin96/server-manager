<template>
  <div class="rounded-xl border shadow bg-white items-center justify-center">
    <template v-if="applicationsCount > 0">
      <div class="flex flex-row items-center justify-start p-6 h-full">
        <template v-if="statusInfo.allUp">
          <font-awesome-icon icon="check-circle" class="text-cyan-400 icon" />
          <div class="text-left">
            <h1 class="font-bold text-xl" v-text="'全部在线'" />
            <p class="text-gray-400 text-xs" v-text="lastUpdate" />
          </div>
        </template>
        <template v-else-if="statusInfo.allDown">
          <font-awesome-icon icon="check-circle" class="text-gray-400 icon" />
          <div class="text-left">
            <h1 class="font-bold text-xl" v-text="'全部离线'" />
            <p class="text-gray-400 text-xs" v-text="lastUpdate" />
          </div>
        </template>
        <template v-if="statusInfo.allUnknown">
          <font-awesome-icon
            icon="question-circle"
            class="text-gray-300 icon"
          />
          <div class="text-left">
            <h1 class="font-bold text-xl" v-text="'全部未知'" />
            <p class="text-gray-400 text-xs" v-text="lastUpdate" />
          </div>
        </template>
        <template v-else-if="someInstancesDown">
          <font-awesome-icon icon="minus-circle" class="text-red-500 icon" />
          <div class="text-left">
            <h1 class="font-bold text-xl" v-text="'部分离线'" />
            <p class="text-gray-400 text-xs" v-text="lastUpdate" />
          </div>
        </template>
        <template v-else-if="someInstancesUnknown">
          <font-awesome-icon
            icon="question-circle"
            class="text-gray-300 icon"
          />
          <div class="text-left">
            <h1 class="font-bold text-xl" v-text="'部分未知'" />
            <p class="text-gray-400 text-xs" v-text="lastUpdate" />
          </div>
        </template>
      </div>
    </template>
    <template v-else>
      <div class="flex flex-row items-center justify-start p-6 h-full">
        <font-awesome-icon icon="frown-open" class="icon text-gray-500" />
        <h1 class="font-bold text-xl" v-text="'暂无应用注册'" />
      </div>
    </template>
  </div>
</template>

<script>
import { computed } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { getStatusInfo } from '@/services/application';

const options = {
  year: 'numeric',
  month: 'numeric',
  day: 'numeric',
  hour: 'numeric',
  minute: 'numeric',
  second: 'numeric',
};

export default {
  setup() {
    const { applications } = useApplicationStore();

    const statusInfo = computed(() => getStatusInfo(applications.value));

    return { applications, statusInfo };
  },
  data() {
    return {
      lastUpdate: new Date(),
      dateTimeFormat: new Intl.DateTimeFormat(this.$i18n.locale, options),
    };
  },
  computed: {
    someInstancesDown() {
      return this.statusInfo.someDown;
    },
    someInstancesUnknown() {
      return this.statusInfo.someUnknown;
    },
    notUpCount() {
      return this.applications.reduce((current, next) => {
        return (
          current +
          next.instances.filter(
            (instance) => instance.statusInfo.status !== 'UP',
          ).length
        );
      }, 0);
    },
    applicationsCount() {
      return this.applications.length;
    },
  },
  beforeMount() {
    this.updateLastUpdateTime();
  },
  methods: {
    updateLastUpdateTime() {
      this.lastUpdate = this.dateTimeFormat.format(new Date());
    },
  },
};
</script>

<style scoped>
.icon {
  @apply text-6xl mx-8;
}
</style>
