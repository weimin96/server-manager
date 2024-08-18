<template>
  <table class="w-full">
    <tbody>
      <tr v-for="logger in loggers.slice(0, visibleLimit)" :key="logger.name">
        <td class="w-9/12">
          <span class="break-all" v-text="logger.name" />&nbsp;
          <sm-tag
            v-if="logger.isNew"
            class="tag is-primary is-uppercase"
            :value="$t('instances.loggers.new')"
          />
        </td>
        <td class="w-1/4">
          <sm-logger-control
            class="is-pulled-right"
            :level-options="levels"
            :value="logger.level"
            :status="loggersStatus[logger.name]"
            :allow-reset="logger.name !== 'ROOT'"
            @input="(level) => $emit('configureLogger', { logger, level })"
          />

          <p
            v-if="
              loggersStatus[logger.name] &&
              loggersStatus[logger.name].status === 'failed'
            "
            class="has-text-danger"
          >
            <font-awesome-icon
              class="has-text-danger"
              icon="exclamation-triangle"
            />
            <span
              v-text="
                $t('instances.loggers.setting_loglevel_failed', {
                  logger: logger.name,
                  loglevel: loggersStatus[logger.name].level,
                })
              "
            />
          </p>
        </td>
      </tr>
      <tr v-if="loggers.length === 0">
        <td
          class="is-muted"
          colspan="5"
          v-text="$t('instances.loggers.no_loggers_found')"
        />
      </tr>
    </tbody>
    <InfiniteLoading
      ref="infiniteLoading"
      :identifier="loggers"
      @infinite="increaseScroll"
    >
      <template #complete>
        <span />
      </template>
    </InfiniteLoading>
  </table>
</template>

<script>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import InfiniteLoading from 'v3-infinite-loading';

import 'v3-infinite-loading/lib/style.css';

import SmTag from '@/components/sm-tag';

import SmLoggerControl from '@/views/instances/loggers/logger-control';

export default {
  components: { FontAwesomeIcon, SmTag, InfiniteLoading, SmLoggerControl },
  props: {
    levels: {
      type: Array,
      required: true,
    },
    loggers: {
      type: Array,
      required: true,
    },
    loggersStatus: {
      type: Object,
      required: true,
    },
  },
  emits: ['configureLogger'],
  data: () => ({
    visibleLimit: 25,
  }),
  methods: {
    increaseScroll($state) {
      if (this.visibleLimit < this.loggers.length) {
        this.visibleLimit += 25;
        $state.loaded();
      } else {
        $state.complete();
      }
    },
  },
};
</script>
