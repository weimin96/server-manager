<template>
  <sm-panel v-if="hasLoaded" :title="$t('instances.details.threads.title')">
    <div>
      <div v-if="current" class="flex w-full">
        <div class="flex-1 text-center">
          <p class="font-bold" v-text="$t('instances.details.threads.live')" />
          <p v-text="current.live" />
        </div>
        <div class="flex-1 text-center">
          <p
            class="font-bold"
            v-text="$t('instances.details.threads.daemon')"
          />
          <p v-text="current.daemon" />
        </div>
        <div class="flex-1 text-center">
          <p
            class="font-bold"
            v-text="$t('instances.details.threads.peak_live')"
          />
          <p v-text="current.peak" />
        </div>
      </div>

      <threads-chart v-if="chartData.length > 0" :data="chartData" />
    </div>
  </sm-panel>
</template>

<script>
import moment from 'moment';
import { take } from 'rxjs/operators';

import SmConfig from '@/main/config';
import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import threadsChart from '@/views/instances/details/threads-chart';

export default {
  components: { threadsChart },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    current: null,
    chartData: [],
  }),
  methods: {
    async fetchMetrics() {
      const responseLive = this.instance.fetchMetric('jvm.threads.live');
      const responsePeak = this.instance.fetchMetric('jvm.threads.peak');
      const responseDaemon = this.instance.fetchMetric('jvm.threads.daemon');

      return {
        live: (await responseLive).data.measurements[0].value,
        peak: (await responsePeak).data.measurements[0].value,
        daemon: (await responseDaemon).data.measurements[0].value,
      };
    },
    createSubscription() {
      return timer(0, SmConfig.uiSettings.pollTimer.threads)
        .pipe(
          concatMap(this.fetchMetrics),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(5));
          }),
        )
        .subscribe({
          next: (data) => {
            this.hasLoaded = true;
            this.current = data;
            this.chartData.push({ ...data, timestamp: moment().valueOf() });
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching threads metrics failed:', error);
            ElMessage.error('加载失败');
          },
        });
    },
  },
};
</script>

<style lang="css">
.threads-current {
  margin-bottom: 0 !important;
}
</style>
