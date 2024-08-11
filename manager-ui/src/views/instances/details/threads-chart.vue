

<template>
  <LineChart
    label="timestamp"
    :datasets="datasets"
    :config="config"
    :data="data"
  />
</template>

<script>
import moment from 'moment';
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
      datasets: {
        live: {
          label: this.t('instances.details.threads.live'),
        },
        daemon: {
          label: this.t('instances.details.threads.daemon'),
        },
      },
      config: {
        options: {
          plugins: {
            tooltip: {
              callbacks: {
                title: function (ctx) {
                  return ctx[0].parsed.y;
                },
                label: function (ctx) {
                  return ctx.dataset.label;
                },
              },
            },
          },
          scales: {
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
