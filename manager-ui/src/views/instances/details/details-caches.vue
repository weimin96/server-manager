

<template>
  <div>
    <details-cache
      v-for="cache in caches"
      :key="cache"
      :cache-name="cache"
      :instance="instance"
    />
  </div>
</template>

<script>
import { uniq } from 'lodash-es';
import { take } from 'rxjs/operators';

import subscribing from '@/mixins/subscribing';
import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import detailsCache from '@/views/instances/details/details-cache';

export default {
  components: { detailsCache },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    caches: [],
  }),
  methods: {
    async fetchCaches() {
      const response = await this.instance.fetchMetric('cache.gets');
      return uniq(
        response.data.availableTags.filter((tag) => tag.tag === 'name')[0]
          .values,
      );
    },
    createSubscription() {
      return timer(0, sbaConfig.uiSettings.pollTimer.cache)
        .pipe(
          concatMap(this.fetchCaches),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(5));
          }),
        )
        .subscribe({
          next: (names) => {
            this.caches = names;
          },
          error: (error) => {
            console.warn('Fetching caches failed:', error);
          },
        });
    },
  },
};
</script>
