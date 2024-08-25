<template>
  <LineChart
    :config="config"
    :data="data"
    :datasets="datasets"
    label="timestamp"
  />
</template>

<script>
import moment from 'moment';
import prettyBytes from 'pretty-bytes';
import { useI18n } from 'vue-i18n';

import LineChart from '@/views/instances/details/LineChart';

export default {
  components: { LineChart },
  props: {
    data: {
      type: Array,
      default: () => [],
    },
  },
  setup(props) {
    const { t } = useI18n();
    return { ...props, t };
  },
  data() {
    return {
      chart: undefined,
      label: 'timestamp',
      datasets: {
        used: {
          label: '已用内存',
        },
        metaspace: {
          label: '元空间内存',
        },
        committed: {
          label: '分配内存',
        },
      },
      config: {
        options: {
          plugins: {
            tooltip: {
              callbacks: {
                title: (ctx) => {
                  return prettyBytes(ctx[0].parsed.y);
                },
                label: (ctx) => {
                  return this.t(ctx.dataset.label);
                },
              },
            },
          },
          scales: {
            y: {
              ticks: {
                callback: (label) => {
                  return prettyBytes(label);
                },
              },
            },
            x: {
              ticks: {
                callback: (label) => {
                  return moment(label).format('HH:mm:ss');
                },
              },
            },
          },
        },
      },
    };
  },
};
</script>
