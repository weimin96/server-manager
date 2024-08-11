

<template>
  <div>
    <details-datasource
      v-for="dataSource in dataSources"
      :key="dataSource"
      :data-source="dataSource"
      :instance="instance"
    />
  </div>
</template>

<script>
import { take } from 'rxjs/operators';

import subscribing from '@/mixins/subscribing';
import sbaConfig from '@/sba-config';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import detailsDatasource from '@/views/instances/details/details-datasource';

export default {
  components: { detailsDatasource },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    dataSources: [],
  }),
  methods: {
    async fetchDataSources() {
      const response = await this.instance.fetchMetric(
        'data.source.active.connections',
      );
      return response.data.availableTags.filter((tag) => tag.tag === 'name')[0]
        .values;
    },
    createSubscription() {
      return timer(0, sbaConfig.uiSettings.pollTimer.datasource)
        .pipe(
          concatMap(this.fetchDataSources),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(5));
          }),
        )
        .subscribe({
          next: (names) => {
            this.dataSources = names;
          },
          error: (error) => {
            console.warn('Fetching datasources failed:', error);
          },
        });
    },
  },
};
</script>
