<template>
  <div class="h-full">
    <sba-wave />
    <div class="h-full">
      <Sidebar
        v-if="instance"
        :key="instanceId"
        :application="application"
        :instance="instance"
        :views="sidebarViews"
      />
      <main class="min-h-full relative z-0 ml-10 md:ml-60 transition-all">
        <router-view
          v-if="instance"
          :application="application"
          :instance="instance"
        />
      </main>
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue';

import { useViewRegistry } from '@/composables/ViewRegistry';
import { useApplicationStore } from '@/composables/useApplicationStore';
import { findApplicationForInstance, findInstance } from '@/store';
import Sidebar from '@/views/instances/shell/sidebar';

export default defineComponent({
  components: {
    Sidebar,
  },
  setup() {
    const { applications } = useApplicationStore();
    const { views } = useViewRegistry();
    return {
      views,
      applications,
    };
  },
  data() {
    return {
      instanceId: this.$route.params.instanceId,
      background: {},
    };
  },
  computed: {
    sidebarViews() {
      return this.views.filter((v) => v.parent === this.activeMainViewName);
    },
    instance() {
      return findInstance(this.applications, this.instanceId);
    },
    application() {
      return findApplicationForInstance(this.applications, this.instanceId);
    },
    activeMainViewName() {
      const currentView = this.$route.meta.view;
      return currentView && (currentView.parent || currentView.name);
    },
  },
  watch: {
    $route: {
      immediate: true,
      handler() {
        this.instanceId = this.$route.params.instanceId;
      },
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances',
      path: '/instances/:instanceId',
      component: this,
      props: true,
      isEnabled() {
        return false;
      },
    });
  },
});
</script>
