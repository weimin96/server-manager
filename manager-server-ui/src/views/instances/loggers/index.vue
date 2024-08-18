<template>
  <loggers
    :instance-count="application.instances.length"
    :loggers-service="service"
    @change-scope="changeScope"
  />
</template>

<script>
import Application from '@/services/application';
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import Loggers from '@/views/instances/loggers/loggers';
import {
  ApplicationLoggers,
  InstanceLoggers,
} from '@/views/instances/loggers/service';

export default {
  components: { Loggers },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    application: {
      type: Application,
      required: true,
    },
  },
  data: function () {
    return {
      scope: this.application.instances.length > 1 ? 'application' : 'instance',
    };
  },
  computed: {
    service() {
      return this.scope === 'instance'
        ? new InstanceLoggers(this.instance)
        : new ApplicationLoggers(this.application);
    },
  },
  methods: {
    changeScope(scope) {
      this.scope = scope;
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/loggers',
      parent: 'instances',
      path: 'loggers',
      label: 'instances.loggers.label',
      component: this,
      group: VIEW_GROUP.LOGGING,
      order: 300,
      isEnabled: ({ instance }) => instance.hasEndpoint('loggers'),
    });
  },
};
</script>
