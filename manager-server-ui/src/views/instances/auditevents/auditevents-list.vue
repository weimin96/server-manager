<template>
  <table class="auditevents table w-full">
    <thead>
      <tr>
        <th v-html="$t('instances.auditevents.timestamp')" />
        <th v-html="$t('instances.auditevents.event')" />
        <th v-html="$t('instances.auditevents.principal')" />
        <th v-html="$t('instances.auditevents.remote_address')" />
        <th v-html="$t('instances.auditevents.session_id')" />
      </tr>
    </thead>
    <tbody>
      <template v-for="event in events" :key="event.key">
        <tr
          class="is-selectable"
          :class="{ 'auditevents__event--is-detailed': showDetails[event.key] }"
          @click="
            showDetails[event.key]
              ? delete showDetails[event.key]
              : (showDetails[event.key] = true)
          "
        >
          <td v-text="event.timestamp.format('L HH:mm:ss.SSS')" />
          <td>
            <span
              class="tag"
              :class="{
                'is-success': event.isSuccess(),
                'is-danger': event.isFailure(),
              }"
              v-text="event.type"
            />
          </td>
          <td v-if="hasSessionEndpoint && event.principal">
            <router-link
              :to="{
                name: 'instances/sessions',
                params: { instanceId: instance.id },
                query: { username: event.principal },
              }"
              v-text="event.principal"
            />
          </td>
          <td v-else v-text="event.principal" />
          <td v-text="event.remoteAddress" />
          <td v-if="hasSessionEndpoint && event.sessionId">
            <router-link
              :to="{
                name: 'instances/sessions',
                params: { instanceId: instance.id },
                query: { sessionId: event.sessionId },
              }"
              v-text="event.sessionId"
            />
          </td>
          <td v-else v-text="event.sessionId" />
        </tr>
        <tr v-if="showDetails[event.key]" :key="`${event.key}-detail`">
          <td colspan="5">
            <pre
              class="auditevents__event-detail"
              v-text="toJson(event.data)"
            />
          </td>
        </tr>
      </template>
      <tr v-if="events.length === 0">
        <td class="is-muted" colspan="5">
          <p
            v-if="isLoading"
            class="is-loading"
            v-html="$t('instances.auditevents.loading_audit_events')"
          />
          <p
            v-else
            v-html="$t('instances.auditevents.no_audit_events_found')"
          />
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script>
import prettyBytes from 'pretty-bytes';

import Instance from '@/services/instance';

export default {
  props: {
    events: {
      type: Array,
      default: () => [],
    },
    instance: {
      type: Instance,
      required: true,
    },
    isLoading: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    showDetails: {},
  }),
  computed: {
    hasSessionEndpoint() {
      return this.instance.hasEndpoint('sessions');
    },
  },
  methods: {
    prettyBytes,
    toJson(obj) {
      return JSON.stringify(obj, null, 4);
    },
  },
};
</script>

<style lang="css">
.auditevents {
  table-layout: fixed;
}
.auditevents td {
  vertical-align: middle;
}
.auditevents__event--is-detailed td {
  border: none !important;
}
.auditevents__event-detail {
  overflow: auto;
}
</style>
