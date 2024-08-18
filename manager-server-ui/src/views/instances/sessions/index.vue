<template>
  <sm-instance-section :error="error" :loading="isLoading">
    <template #before>
      <sm-sticky-subnav>
        <div class="flex -space-x-px">
          <select
            v-model="filter.type"
            class="relative focus:z-10 focus:ring-indigo-500 focus:border-indigo-500 block sm:text-sm border-gray-300 rounded-md rounded-r-none"
          >
            <option value="username" v-text="$t('term.username')" />
            <option
              value="sessionId"
              v-text="$t('instances.sessions.session_id')"
            />
          </select>
          <sm-input
            v-model="filter.value"
            input-class="!rounded-l-none"
            name="filter"
            type="search"
            @paste="handlePaste"
            @keyup.enter="fetchSessionsByUsername()"
          />
        </div>
      </sm-sticky-subnav>
    </template>

    <sm-panel :seamless="true">
      <sm-sessions-list
        :instance="instance"
        :is-loading="isLoading"
        :sessions="sessions"
        @deleted="fetch"
      />
    </sm-panel>
  </sm-instance-section>
</template>

<script>
import { debounce, isEqual } from 'lodash-es';
import moment from 'moment';

import SmPanel from '@/components/sm-panel';
import SmStickySubnav from '@/components/sm-sticky-subnav';

import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SmSessionsList from '@/views/instances/sessions/sessions-list';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

const regexUuid =
  /[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/;

class Session {
  constructor({ creationTime, lastAccessedTime, ...session }) {
    Object.assign(this, session);
    this.creationTime = moment(creationTime);
    this.lastAccessedTime = moment(lastAccessedTime);
  }
}

export default {
  components: {
    SmPanel,
    SmStickySubnav,
    SmInstanceSection,
    SmSessionsList,
  },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    error: null,
    filter: { value: '', type: null },
    sessions: [],
    isLoading: false,
    currentRouteName: null,
  }),
  watch: {
    '$route.query': {
      immediate: true,
      handler() {
        this.filter = Object.entries(this.$route.query).reduce(
          (acc, [name, value]) => {
            acc.type = name;
            acc.value = value;
            return acc;
          },
          { type: 'username', value: '' },
        );
      },
    },
    filter: {
      deep: true,
      immediate: true,
      handler() {
        const oldQuery = { [this.filter.type]: this.filter.value };
        if (
          !isEqual(oldQuery, this.$route.query) &&
          this.currentRouteName === this.$route.name
        ) {
          this.$router.replace({
            name: 'instances/sessions',
            query: oldQuery,
          });
        }
        this.fetch();
      },
    },
  },
  mounted() {
    this.currentRouteName = this.$route.name;
  },
  methods: {
    fetch: debounce(async function () {
      this.error = null;
      if (!this.filter.value) {
        this.sessions = [];
        return;
      }

      this.isLoading = true;
      try {
        if (this.filter.type === 'sessionId') {
          this.sessions = await this.fetchSession();
        } else {
          this.sessions = await this.fetchSessionsByUsername();
        }
      } catch (error) {
        console.warn('Fetching sessions failed:', error);
        this.error = error;
      }
      this.isLoading = false;
    }, 250),
    async fetchSession() {
      try {
        const response = await this.instance.fetchSession(this.filter.value);
        return [new Session(response.data)];
      } catch (error) {
        if (error.response.status === 404) {
          return [];
        } else {
          throw error;
        }
      }
    },
    async fetchSessionsByUsername() {
      const response = await this.instance.fetchSessionsByUsername(
        this.filter.value,
      );
      return response.data.sessions.map((session) => new Session(session));
    },
    handlePaste(event) {
      const looksLikeSessionId = event.clipboardData
        .getData('text')
        .match(regexUuid);
      if (looksLikeSessionId) {
        this.filter.type = 'sessionId';
      }
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/sessions',
      parent: 'instances',
      path: 'sessions',
      component: this,
      label: 'instances.sessions.label',
      group: VIEW_GROUP.SECURITY,
      order: 700,
      isEnabled: ({ instance }) => instance.hasEndpoint('sessions'),
    });
  },
};
</script>
