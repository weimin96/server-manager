<template>
  <sm-panel v-if="hasLoaded" :title="`Cache: ${cacheName}`">
    <div>
      <div v-if="current" class="level cache-current">
        <div
          v-if="current.hit !== undefined"
          class="level-item has-text-centered"
        >
          <div>
            <p
              class="heading has-bullet has-bullet-info"
              v-text="$t('instances.details.cache.hits')"
            />
            <p v-text="current.hit" />
          </div>
        </div>
        <div
          v-if="current.miss !== undefined"
          class="level-item has-text-centered"
        >
          <div>
            <p
              class="heading has-bullet has-bullet-warning"
              v-text="$t('instances.details.cache.misses')"
            />
            <p v-text="current.miss" />
          </div>
        </div>
        <div v-if="ratio !== undefined" class="level-item has-text-centered">
          <div>
            <p
              class="heading"
              v-text="$t('instances.details.cache.hit_ratio')"
            />
            <p v-text="ratio" />
          </div>
        </div>
        <div
          v-if="current.size !== undefined"
          class="level-item has-text-centered"
        >
          <div>
            <p class="heading" v-text="$t('instances.details.cache.size')" />
            <p v-text="current.size" />
          </div>
        </div>
      </div>
      <cache-chart v-if="chartData.length > 0" :data="chartData" />
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
import cacheChart from '@/views/instances/details/cache-chart';

export default {
  components: { cacheChart },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    cacheName: {
      type: String,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    current: null,
    shouldFetchCacheSize: true,
    shouldFetchCacheHits: true,
    shouldFetchCacheMisses: true,
    chartData: [],
  }),
  computed: {
    ratio() {
      if (Number.isFinite(this.current.hit) && Number.isFinite(this.current)) {
        const total = this.current.hit + this.current.miss;
        return total > 0
          ? ((this.current.hit / total) * 100).toFixed(2) + '%'
          : '-';
      }
      return undefined;
    },
  },
  methods: {
    async fetchMetrics() {
      const [hit, miss, size] = await Promise.all([
        this.fetchCacheHits(),
        this.fetchCacheMisses(),
        this.fetchCacheSize(),
      ]);
      return {
        hit: hit,
        miss: miss,
        total: hit + (miss || 0),
        size,
      };
    },
    async fetchCacheHits() {
      if (this.shouldFetchCacheHits) {
        try {
          const response = await this.instance.fetchMetric('cache.gets', {
            name: this.cacheName,
            result: 'hit',
          });
          return response.data.measurements[0].value;
        } catch (error) {
          this.shouldFetchCacheHits = false;
          console.warn(
            `Fetching cache ${this.cacheName} hits failed - error is ignored`,
            error,
          );
          return undefined;
        }
      }
    },
    async fetchCacheMisses() {
      if (this.shouldFetchCacheMisses) {
        try {
          const response = await this.instance.fetchMetric('cache.gets', {
            name: this.cacheName,
            result: 'miss',
          });
          return response.data.measurements[0].value;
        } catch (error) {
          this.shouldFetchCacheMisses = false;
          console.warn(
            `Fetching cache ${this.cacheName} misses failed - error is ignored`,
            error,
          );
          return undefined;
        }
      }
    },
    async fetchCacheSize() {
      if (this.shouldFetchCacheSize) {
        try {
          const response = await this.instance.fetchMetric('cache.size', {
            name: this.cacheName,
          });
          return response.data.measurements[0].value;
        } catch (error) {
          this.shouldFetchCacheSize = false;
          console.warn(
            `Fetching cache ${this.cacheName} size failed - error is ignored`,
            error,
          );
          return undefined;
        }
      }
    },
    createSubscription() {
      return timer(0, SmConfig.uiSettings.pollTimer.cache)
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
            console.warn(
              `Fetching cache ${this.cacheName} metrics failed:`,
              error,
            );
            ElMessage.error('加载失败');
          },
        });
    },
  },
};
</script>

<style lang="css">
.datasource-current {
  margin-bottom: 0 !important;
}
</style>
