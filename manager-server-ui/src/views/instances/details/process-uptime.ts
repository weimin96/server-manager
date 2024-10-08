import moment from 'moment';
import { h } from 'vue';

import subscribing from '../../../mixins/subscribing.js';

import { timer } from '@/utils/rxjs';

export default {
  props: ['value'],
  mixins: [subscribing],
  data: () => ({
    startTs: null,
    offset: null,
  }),
  render() {
    return h('span', this.clock);
  },
  computed: {
    clock() {
      if (!this.value) {
        return null;
      }
      const duration = moment.duration(this.value + this.offset);
      return `${Math.floor(
        duration.asDays(),
      )}天 ${duration.hours()}小时 ${duration.minutes()}分 ${duration.seconds()}秒`;
    },
  },
  watch: {
    value: 'subscribe',
  },
  methods: {
    createSubscription() {
      if (this.value) {
        this.startTs = moment();
        this.offset = 0;
        return timer(0, 1000).subscribe({
          next: () => {
            this.offset = moment().valueOf() - this.startTs.valueOf();
          },
        });
      }
    },
  },
};
