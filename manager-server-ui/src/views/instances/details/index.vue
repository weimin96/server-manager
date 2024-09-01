<template>
  <sm-instance-section :loading="!hasLoaded">
    <template #before>
      <details-hero :instance="instance" />
    </template>

    <div class="flex gap-6 flex-col lg:flex-row">
      <div class="flex-1">
        <details-info v-if="hasInfo" :instance="instance" />
        <details-metadata v-if="hasMetadata" :instance="instance" />
      </div>
      <div class="flex-1">
        <details-health :instance="instance" />
      </div>
    </div>

    <div class="flex gap-6 flex-col lg:flex-row">
      <div class="flex-1">
        <details-process
          v-if="hasProcess"
          :instance="instance"
          class="break-inside-avoid"
        />
        <details-gc v-if="hasGc" :instance="instance" />
      </div>
      <div class="flex-1">
        <details-threads v-if="hasThreads" :instance="instance" />
      </div>
    </div>

    <div class="flex gap-6 flex-col lg:flex-row">
      <div class="flex-1">
        <details-memory v-if="hasMemory" :instance="instance" type="heap" />
      </div>
      <div class="flex-1">
        <details-memory v-if="hasMemory" :instance="instance" type="nonheap" />
      </div>
    </div>

    <div class="flex gap-6 flex-col lg:flex-row">
      <div class="flex-1">
        <details-datasources v-if="hasDatasources" :instance="instance" />
      </div>
      <div class="flex-1">
        <details-caches v-if="hasCaches" :instance="instance" />
      </div>
    </div>
  </sm-instance-section>
</template>

<script>
import Application from '@/services/application';
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import detailsCaches from '@/views/instances/details/details-caches';
import detailsDatasources from '@/views/instances/details/details-datasources';
import detailsGc from '@/views/instances/details/details-gc';
import detailsHealth from '@/views/instances/details/details-health';
import DetailsHero from '@/views/instances/details/details-hero';
import detailsInfo from '@/views/instances/details/details-info';
import detailsMemory from '@/views/instances/details/details-memory';
import detailsMetadata from '@/views/instances/details/details-metadata';
import detailsProcess from '@/views/instances/details/details-process';
import detailsThreads from '@/views/instances/details/details-threads';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

export default {
  components: {
    SmInstanceSection,
    DetailsHero,
    detailsHealth,
    detailsInfo,
    detailsProcess,
    detailsThreads,
    detailsDatasources,
    detailsMemory,
    detailsGc,
    detailsCaches,
    detailsMetadata,
  },
  props: {
    application: {
      type: Application,
      default: () => ({}),
    },
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: true,
    error: null,
    metrics: [],
  }),
  computed: {
    hasCaches() {
      return this.hasMetric('cache.gets');
    },
    hasDatasources() {
      return this.hasMetric('data.source.active.connections');
    },
    hasGc() {
      return this.hasMetric('jvm.gc.pause');
    },
    hasInfo() {
      return this.instance.hasEndpoint('info');
    },
    hasMemory() {
      return this.hasMetric('jvm.memory.max');
    },
    hasProcess() {
      return this.hasMetric('process.uptime');
    },
    hasThreads() {
      return this.hasMetric('jvm.threads.live');
    },
    hasMetadata() {
      return this.instance.registration && this.instance.registration.metadata;
    },
  },
  watch: {
    instance() {
      this.fetchMetricIndex();
    },
  },
  created() {
    this.fetchMetricIndex();
  },
  methods: {
    hasMetric(metric) {
      return this.metrics && this.metrics.includes(metric);
    },
    async fetchMetricIndex() {
      if (this.instance.hasEndpoint('metrics')) {
        this.hasLoaded = false;
        this.error = null;
        try {
          const res = await this.instance.fetchMetrics();
          this.metrics = res.data.names;
        } catch (error) {
          console.warn('Fetching metric index failed:', error);
          this.error = error;
        } finally {
          this.hasLoaded = true;
        }
      }
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/details',
      parent: 'instances',
      path: 'details',
      component: this,
      label: 'instances.details.label',
      group: VIEW_GROUP.INSIGHTS,
      order: 0,
    });
  },
};
</script>
