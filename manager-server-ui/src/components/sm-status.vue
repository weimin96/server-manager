<template>
  <div :aria-label="status" class="application-status">
    <font-awesome-icon
      :class="`application-status__icon--${status}`"
      :icon="icon"
      class="application-status__icon"
    />
    <small v-if="date" class="hidden md:block">
      <sm-time-ago :date="date" />
    </small>
  </div>
</template>

<script>
import moment from 'moment';

import SmTimeAgo from '@/components/sm-time-ago';

const icons = {
  UP: 'check-circle',
  RESTRICTED: 'exclamation',
  OUT_OF_SERVICE: 'ban',
  DOWN: 'times-circle',
  OFFLINE: 'minus-circle',
  UNKNOWN: 'question-circle',
};

export default {
  components: { SmTimeAgo },
  props: {
    status: {
      type: String,
      default: 'UNKNOWN',
    },
    date: {
      type: [String, Date, Number, moment],
      default: null,
    },
  },
  computed: {
    icon() {
      return icons[this.status];
    },
  },
};
</script>

<style>
.application-status {
  @apply text-center inline-flex flex-col;
}
.application-status__icon {
  @apply text-gray-500 mx-auto;
}
.application-status__icon--UP {
  color: #67e8f9;
}
.application-status__icon--RESTRICTED {
  color: #ffe08a;
}
.application-status__icon--OUT_OF_SERVICE,
.application-status__icon--DOWN {
  color: #f14668;
}
.application-status__icon--UNKNOWN,
.application-status__icon--OFFLINE {
  color: #7a7a7a;
}
</style>
