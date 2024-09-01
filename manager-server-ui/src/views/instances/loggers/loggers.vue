<template>
  <sm-instance-section :loading="!hasLoaded">
    <template #before>
      <sm-sticky-subnav>
        <div class="flex gap-2">
          <sm-toggle-scope-button
            v-if="instanceCount >= 1"
            v-model="scope"
            :instance-count="instanceCount"
            :show-info="false"
            @update:model-value="$emit('changeScope', $event)"
          />

          <div class="flex-1">
            <sm-input
              v-model="filter.name"
              :placeholder="$t('term.filter')"
              name="filter"
              type="search"
            >
              <template #prepend>
                <font-awesome-icon icon="filter" />
              </template>
              <template #append>
                <span v-text="filteredLoggers.length" /> /
                <span v-text="loggerConfig.loggers.length" />
              </template>
            </sm-input>
          </div>

          <!-- FILTER -->
          <div>
            <div class="flex items-start">
              <div class="flex items-center h-5">
                <input
                  id="classOnly"
                  v-model="filter.classOnly"
                  class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
                  name="wraplines"
                  type="checkbox"
                />
              </div>
              <div class="ml-3 text-sm">
                <label
                  class="font-medium text-gray-700"
                  for="classOnly"
                  v-text="$t('instances.loggers.filter.class_only')"
                />
              </div>
            </div>

            <div class="flex items-start">
              <div class="flex items-center h-5">
                <input
                  id="configuredOnly"
                  v-model="filter.configuredOnly"
                  class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
                  name="wraplines"
                  type="checkbox"
                />
              </div>
              <div class="ml-3 text-sm">
                <label
                  class="font-medium text-gray-700"
                  for="configuredOnly"
                  v-text="$t('instances.loggers.filter.configured')"
                />
              </div>
            </div>
          </div>
          <!-- // FILTER -->
        </div>
      </sm-sticky-subnav>
    </template>

    <sm-panel>
      <loggers-list
        :levels="loggerConfig.levels"
        :loggers="filteredLoggers"
        :loggers-status="loggersStatus"
        @configure-logger="
          ({ logger, level }) => configureLogger(logger, level)
        "
      />
    </sm-panel>
  </sm-instance-section>
</template>

<script>
import { ActionScope } from '@/components/ActionScope';

import { finalize, from, listen } from '@/utils/rxjs';
import LoggersList from '@/views/instances/loggers/loggers-list';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

const isClassName = (name) => /\.[A-Z]/.test(name);

const addToFilter = (oldFilter, addedFilter) =>
  !oldFilter
    ? addedFilter
    : (val, key) => oldFilter(val, key) && addedFilter(val, key);

const addLoggerCreationEntryIfLoggerNotPresent = (nameFilter, loggers) => {
  if (nameFilter && !loggers.some((logger) => logger.name === nameFilter)) {
    loggers.unshift({
      level: [
        {
          configuredLevel: null,
          effectiveLevel: null,
          instanceId: null,
        },
      ],
      name: nameFilter,
      isNew: true,
    });
  }
};

export default {
  components: { SmInstanceSection, LoggersList },
  props: {
    instanceCount: {
      type: Number,
      required: true,
    },
    loggersService: {
      type: Object,
      required: true,
    },
  },
  emits: ['changeScope'],
  data() {
    return {
      hasLoaded: false,
      failedInstances: 0,
      loggerConfig: { loggers: [], levels: [] },
      filter: {
        name: '',
        classOnly: false,
        configuredOnly: false,
      },
      loggersStatus: {},
      scope:
        this.instanceCount > 1 ? ActionScope.APPLICATION : ActionScope.INSTANCE,
    };
  },
  computed: {
    filteredLoggers() {
      const filterFn = this.getFilterFn();
      const filteredLoggers = filterFn
        ? this.loggerConfig.loggers.filter(filterFn)
        : this.loggerConfig.loggers;
      addLoggerCreationEntryIfLoggerNotPresent(
        this.filter.name,
        filteredLoggers,
      );
      return filteredLoggers;
    },
  },
  watch: {
    loggersService: {
      immediate: true,
      handler() {
        return this.fetchLoggers();
      },
    },
  },
  methods: {
    configureLogger(logger, level) {
      from(this.loggersService.configureLogger(logger.name, level))
        .pipe(
          listen(
            (status) => (this.loggersStatus[logger.name] = { level, status }),
          ),
          finalize(() => this.fetchLoggers()),
        )
        .subscribe({
          error: (error) =>
            console.warn(`Configuring logger '${logger.name}' failed:`, error),
        });
    },
    async fetchLoggers() {
      this.failedInstances = 0;
      try {
        const { ...loggerConfig } = await this.loggersService.fetchLoggers();
        this.loggerConfig = Object.freeze(loggerConfig);
      } catch (error) {
        ElMessage.error('加载失败');
        console.warn('Fetching loggers failed:', error);
      }
      this.hasLoaded = true;
    },
    getFilterFn() {
      let filterFn = null;

      if (this.filter.classOnly) {
        filterFn = addToFilter(filterFn, (logger) => isClassName(logger.name));
      }

      if (this.filter.configuredOnly) {
        filterFn = addToFilter(filterFn, (logger) =>
          logger.level.some((l) => Boolean(l.configuredLevel)),
        );
      }

      if (this.filter.name) {
        const normalizedFilter = this.filter.name.toLowerCase();
        filterFn = addToFilter(filterFn, (logger) =>
          logger.name.toLowerCase().includes(normalizedFilter),
        );
      }

      return filterFn;
    },
  },
};
</script>
