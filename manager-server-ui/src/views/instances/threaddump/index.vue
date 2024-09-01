<template>
  <sm-instance-section :error="errorFetch" :loading="!hasLoaded">
    <template v-if="threads" #before>
      <sm-sticky-subnav>
        <div class="text-right">
          <sm-button @click="downloadThreaddump">
            <font-awesome-icon icon="download" />&nbsp;
            <span v-text="$t('instances.threaddump.download')" />
          </sm-button>
        </div>
      </sm-sticky-subnav>
    </template>
    <sm-panel>
      <threads-list v-if="threads" :thread-timelines="threads" />
    </sm-panel>
  </sm-instance-section>
</template>

<script>
import { remove } from 'lodash-es';
import moment from 'moment';
import { take } from 'rxjs/operators';

import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';
import threadsList from '@/views/instances/threaddump/threads-list';

export default {
  components: { SmInstanceSection, threadsList },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    errorFetch: null,
    threads: {},
  }),
  computed: {},
  methods: {
    updateTimelines(threads) {
      const now = moment().valueOf();
      //initialize with all known live threads, which will be removed from the list if still alive
      const terminatedThreads = Object.entries(this.threads)
        .filter(([, value]) => value.threadState !== 'TERMINATED')
        .map(([threadId]) => parseInt(threadId));

      threads.forEach((thread) => {
        if (!this.threads[thread.threadId]) {
          this.threads[thread.threadId] = {
            threadId: thread.threadId,
            threadState: thread.threadState,
            threadName: thread.threadName,
            timeline: [
              {
                start: now,
                end: now,
                details: thread,
                threadState: thread.threadState,
              },
            ],
          };
        } else {
          const entry = this.threads[thread.threadId];
          if (entry.threadState !== thread.threadState) {
            entry.threadState = thread.threadState;
            entry.timeline[entry.timeline.length - 1].end = now;
            entry.timeline.push({
              start: now,
              end: now,
              details: thread,
              threadState: thread.threadState,
            });
          } else {
            entry.timeline[entry.timeline.length - 1].end = now;
          }
        }
        remove(terminatedThreads, (threadId) => threadId === thread.threadId);
      });

      terminatedThreads.forEach((threadId) => {
        const entry = this.threads[threadId];
        entry.threadState = 'TERMINATED';
        entry.timeline[entry.timeline.length - 1].end = now;
      });
    },
    async fetchThreaddump() {
      const response = await this.instance.fetchThreaddump();
      return response.data.threads;
    },
    async downloadThreaddump() {
      try {
        await this.instance.downloadThreaddump();
      } catch (error) {
        console.warn('Downloading thread dump failed.', error);
        ElMessage.error('下载失败');
      }
    },
    createSubscription() {
      this.errorFetch = null;
      return timer(0, 1000)
        .pipe(
          concatMap(this.fetchThreaddump),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(2));
          }),
        )
        .subscribe({
          next: (threads) => {
            this.hasLoaded = true;
            this.updateTimelines(threads);
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching threaddump failed:', error);
            this.errorFetch = error;
          },
        });
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/threaddump',
      parent: 'instances',
      path: 'threaddump',
      label: 'instances.threaddump.label',
      component: this,
      group: VIEW_GROUP.JVM,
      order: 400,
      isEnabled: ({ instance }) => instance.hasEndpoint('threaddump'),
    });
  },
};
</script>
