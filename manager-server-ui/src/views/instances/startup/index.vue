<template>
  <sm-instance-section :error="error">
    <template #before>
      <sm-sticky-subnav>
        <div class="flex gap-2">
          <sm-button v-if="!isExpanded" @click="expandTree">
            <svg
              class="h-4 w-4"
              fill="currentColor"
              viewBox="0 0 16 16"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M1 8a.5.5 0 0 1 .5-.5h13a.5.5 0 0 1 0 1h-13A.5.5 0 0 1 1 8zM7.646.146a.5.5 0 0 1 .708 0l2 2a.5.5 0 0 1-.708.708L8.5 1.707V5.5a.5.5 0 0 1-1 0V1.707L6.354 2.854a.5.5 0 1 1-.708-.708l2-2zM8 10a.5.5 0 0 1 .5.5v3.793l1.146-1.147a.5.5 0 0 1 .708.708l-2 2a.5.5 0 0 1-.708 0l-2-2a.5.5 0 0 1 .708-.708L7.5 14.293V10.5A.5.5 0 0 1 8 10z"
                fill-rule="evenodd"
              />
            </svg>
          </sm-button>
          <sm-button v-if="isExpanded" @click="expandTree">
            <svg
              class="h-4 w-4"
              fill="currentColor"
              viewBox="0 0 16 16"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M1 8a.5.5 0 0 1 .5-.5h13a.5.5 0 0 1 0 1h-13A.5.5 0 0 1 1 8zm7-8a.5.5 0 0 1 .5.5v3.793l1.146-1.147a.5.5 0 0 1 .708.708l-2 2a.5.5 0 0 1-.708 0l-2-2a.5.5 0 1 1 .708-.708L7.5 4.293V.5A.5.5 0 0 1 8 0zm-.5 11.707-1.146 1.147a.5.5 0 0 1-.708-.708l2-2a.5.5 0 0 1 .708 0l2 2a.5.5 0 0 1-.708.708L8.5 11.707V15.5a.5.5 0 0 1-1 0v-3.793z"
                fill-rule="evenodd"
              />
            </svg>
          </sm-button>
          <div class="flex-1">
            <sm-input
              v-model="filter"
              :placeholder="
                $t('term.filter') +
                ' by name/tags(key,value) or number for filter(duration)'
              "
              name="filter"
              type="search"
            >
              <template #prepend>
                <font-awesome-icon icon="filter" />
              </template>
              <template #append>
                <span v-text="filteredSize" /> /
                <span v-text="totalSize" />
              </template>
            </sm-input>
          </div>
        </div>
      </sm-sticky-subnav>
    </template>

    <sm-panel>
      <div class="-mx-4 -my-3">
        <tree-table
          v-if="hasLoaded"
          :expand="expandedNodes"
          :tree="eventTree"
          :filter="filter"
          @after-filter-action="afterFilterAction"
          @change="saveTreeState"
        />
      </div>
    </sm-panel>
  </sm-instance-section>
</template>

<script>
import Instance from '@/services/instance';
import { StartupActuatorService } from '@/services/startup-actuator';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';
import TreeTable from '@/views/instances/startup/tree-table';

export default {
  components: { SmInstanceSection, TreeTable },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    error: null,
    hasLoaded: false,
    expandedNodes: null,
    eventTree: null,
    isExpanded: false,
    filter: null,
    filteredSize: '',
    totalSize: '',
  }),
  async created() {
    await this.fetchStartup();

    this.loadTreeState();
    this.expandEventId();

    this.hasLoaded = true;
  },
  methods: {
    expandTree() {
      if (!this.isExpanded) {
        this.expandedNodes = new Set(
          this.eventTree.getEvents().map((e) => e.startupStep.id),
        );
        this.isExpanded = true;
      } else {
        this.expandedNodes = new Set();
        this.isExpanded = false;
      }
    },
    async fetchStartup() {
      this.error = null;
      try {
        const res = await this.instance.fetchStartup();
        this.eventTree = StartupActuatorService.parseAsTree(res.data);
        this.totalSize = this.eventTree.getEvents().length;
        this.filteredSize = this.totalSize;
      } catch (error) {
        console.warn('Fetching startup failed:', error);
        this.error = error;
      }
    },
    afterFilterAction(filteredSize) {
      this.filteredSize = filteredSize;
    },
    expandEventId() {
      let queryParams = this.$router.currentRoute.query;
      if (queryParams && queryParams.id) {
        this.expandedNodes = new Set(this.eventTree.getPath(+queryParams.id));
      }
    },
    loadTreeState() {
      if (window.localStorage) {
        let value = localStorage.getItem(
          `applications/${this.instance.registration.name}/startup`,
        );
        if (value) {
          let parse = JSON.parse(value);
          this.expandedNodes = new Set(parse.expandedNodes);
        }
      }
    },
    saveTreeState($event) {
      if (window.localStorage) {
        localStorage.setItem(
          `applications/${this.instance.registration.name}/startup`,
          JSON.stringify({
            expandedNodes: [...$event.expandedNodes],
          }),
        );
      }
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/startup',
      parent: 'instances',
      path: 'startup',
      component: this,
      label: 'instances.startup.label',
      group: VIEW_GROUP.LOGGING,
      order: 600,
      isEnabled: ({ instance }) => instance.hasEndpoint('startup'),
    });
  },
};
</script>
