<template>
  <sm-instance-section :loading="!hasLoaded">
    <template #before>
      <sm-sticky-subnav>
        <div class="flex gap-2">
          <sm-input
            v-model="filter.uri"
            :placeholder="$t('term.filter')"
            class="flex-1"
            name="filter"
            type="search"
          >
            <template #prepend>
              <font-awesome-icon icon="filter" />
            </template>
            <template #append>
              <span class="button is-static">
                <span v-text="filteredTraces.length" />
                /
                <span v-text="traces.length" />
              </span>
            </template>
          </sm-input>

          <sm-input v-model="limit" :min="0" class="w-32" type="number">
            <template #prepend>
              {{ $t('instances.httptrace.limit') }}
            </template>
          </sm-input>

          <div class="grid grid-rows-2 grid-flow-col gap-x-2 text-sm">
            <sm-checkbox
              v-model="filter.showSuccess"
              :label="$t('instances.httptrace.filter.success')"
            />
            <sm-checkbox
              v-model="filter.showClientErrors"
              :label="$t('instances.httptrace.filter.client_errors')"
            />
            <sm-checkbox
              v-model="filter.showServerErrors"
              :label="$t('instances.httptrace.filter.server_errors')"
            />
            <sm-checkbox
              v-if="actuatorPath"
              v-model="filter.excludeActuator"
              :label="
                $t('instances.httptrace.filter.exclude_actuator', {
                  actuator: actuatorPath,
                })
              "
            />
          </div>
        </div>
      </sm-sticky-subnav>
    </template>

    <sm-panel>
      <sm-traces-chart
        :traces="filteredTraces"
        class="mb-6"
        @selected="updateSelection"
      />

      <sm-traces-list
        :new-traces-count="newTracesCount"
        :traces="listedTraces"
        @show-new-traces="showNewTraces"
      />
    </sm-panel>
  </sm-instance-section>
</template>

<script>
import { debounce, mapKeys } from 'lodash-es';
import moment from 'moment';
import { take } from 'rxjs/operators';

import SmCheckbox from '@/components/sm-checkbox.vue';

import subscribing from '@/mixins/subscribing';
import Instance from '@/services/instance';
import { concatMap, delay, retryWhen, timer } from '@/utils/rxjs';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SmTracesChart from '@/views/instances/httptrace/traces-chart';
import SmTracesList from '@/views/instances/httptrace/traces-list';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

const addToFilter = (oldFilter, addedFilter) =>
  !oldFilter
    ? addedFilter
    : (val, key) => oldFilter(val, key) && addedFilter(val, key);

const normalize = (obj) => mapKeys(obj, (value, key) => key.toLowerCase());

class Trace {
  constructor({ timestamp, request, response, ...trace }) {
    Object.assign(this, trace);
    this.timestamp = moment(timestamp);
    this.request = { ...request, headers: normalize(request.headers) };
    this.response = response
      ? { ...response, headers: normalize(response.headers) }
      : null;
  }

  get key() {
    return `${this.timestamp.valueOf()}-${this.request.method}-${
      this.request.uri
    }`;
  }

  get contentLengthResponse() {
    return this.extractContentLength(this.response);
  }

  get contentLengthRequest() {
    return this.extractContentLength(this.request);
  }

  get contentTypeResponse() {
    return this.extractContentType(this.response);
  }

  get contentTypeRequest() {
    return this.extractContentType(this.request);
  }

  extractContentLength(origin) {
    const contentLength =
      origin &&
      origin.headers['content-length'] &&
      origin.headers['content-length'][0];
    if (contentLength && /^\d+$/.test(contentLength)) {
      return parseInt(contentLength);
    }
    return null;
  }

  extractContentType(origin) {
    const contentType =
      origin &&
      origin.headers['content-type'] &&
      origin.headers['content-type'][0];
    if (contentType) {
      const idx = contentType.indexOf(';');
      return idx >= 0 ? contentType.substring(0, idx) : contentType;
    }
    return null;
  }

  compareTo(other) {
    return this.timestamp - other.timestamp;
  }

  isPending() {
    return !this.response;
  }

  isSuccess() {
    return this.response && this.response.status <= 399;
  }

  isClientError() {
    return (
      this.response &&
      this.response.status >= 400 &&
      this.response.status <= 499
    );
  }

  isServerError() {
    return (
      this.response &&
      this.response.status >= 500 &&
      this.response.status <= 599
    );
  }
}

export default {
  components: {
    SmCheckbox,
    SmInstanceSection,
    SmTracesList,
    SmTracesChart,
  },
  mixins: [subscribing],
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    traces: [],
    listOffset: 0,
    filter: {
      excludeActuator: true,
      showSuccess: true,
      showClientErrors: true,
      showServerErrors: true,
      uri: null,
    },
    limit: 1000,
    selection: null,
  }),
  computed: {
    actuatorPath() {
      if (
        this.instance.registration.managementUrl.includes(
          this.instance.registration.serviceUrl,
        )
      ) {
        const appendix = this.instance.registration.managementUrl.substring(
          this.instance.registration.serviceUrl.length,
        );
        if (appendix.length > 0) {
          return appendix.startsWith('/') ? appendix : `/${appendix}`;
        }
      }
      return null;
    },
    filteredTraces() {
      return this.filterTraces(this.traces);
    },
    newTracesCount() {
      return this.selection
        ? 0
        : this.filterTraces(this.traces.slice(0, this.listOffset)).length;
    },
    listedTraces() {
      const traces = this.filterTraces(this.traces.slice(this.listOffset));
      if (!this.selection) {
        return traces;
      }
      const [start, end] = this.selection;
      return traces.filter(
        (trace) =>
          !trace.timestamp.isBefore(start) && !trace.timestamp.isAfter(end),
      );
    },
    lastTimestamp() {
      return this.traces.length > 0 ? this.traces[0].timestamp : moment(0);
    },
  },
  watch: {
    limit: debounce(function (value) {
      if (this.traces.length > value) {
        this.traces = Object.freeze(this.traces.slice(0, value));
      }
    }, 250),
  },
  methods: {
    updateSelection(selection) {
      this.selection = selection;
      this.showNewTraces();
    },
    showNewTraces() {
      this.listOffset = 0;
    },
    async fetchHttptrace() {
      const response = await this.instance.fetchHttptrace();
      const traces = response.data.traces
        .map((trace) => new Trace(trace))
        .filter((trace) => trace.timestamp.isAfter(this.lastTimestamp));
      traces.sort((a, b) => -1 * a.compareTo(b));
      return traces;
    },
    createSubscription() {
      return timer(0, 5000)
        .pipe(
          concatMap(this.fetchHttptrace),
          retryWhen((err) => {
            return err.pipe(delay(1000), take(2));
          }),
        )
        .subscribe({
          next: (traces) => {
            this.hasLoaded = true;
            if (this.traces.length > 0) {
              this.listOffset += traces.length;
            }
            this.traces = [...traces, ...this.traces].slice(0, this.limit);
          },
          error: (error) => {
            this.hasLoaded = true;
            console.warn('Fetching traces failed:', error);
            this.error = error;
          },
        });
    },
    filterTraces(traces) {
      let filterFn = null;
      if (this.actuatorPath !== null && this.filter.excludeActuator) {
        filterFn = addToFilter(
          filterFn,
          (trace) => !trace.request.uri.includes(this.actuatorPath),
        );
      }
      if (this.filter.uri) {
        const normalizedFilter = this.filter.uri.toLowerCase();
        filterFn = addToFilter(filterFn, (trace) =>
          trace.request.uri.toLowerCase().includes(normalizedFilter),
        );
      }
      if (!this.filter.showSuccess) {
        filterFn = addToFilter(filterFn, (trace) => !trace.isSuccess());
      }
      if (!this.filter.showClientErrors) {
        filterFn = addToFilter(filterFn, (trace) => !trace.isClientError());
      }
      if (!this.filter.showServerErrors) {
        filterFn = addToFilter(filterFn, (trace) => !trace.isServerError());
      }
      return filterFn ? traces.filter(filterFn) : traces;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/httptrace',
      parent: 'instances',
      path: 'httptrace',
      component: this,
      label: 'instances.httptrace.label',
      group: VIEW_GROUP.WEB,
      order: 500,
      isEnabled: ({ instance }) => instance.hasEndpoint('httptrace'),
    });
  },
};
</script>
<style lang="css">
.httptraces__limit {
  width: 5em;
}
</style>
