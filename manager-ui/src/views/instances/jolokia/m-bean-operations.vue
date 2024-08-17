<template>
  <div v-if="application.instances.length > 1" class="absolute right-0 top-0">
    <sm-toggle-scope-button
      v-model="scope"
      :instance-count="application.instances.length"
      class="bg-white px-4 py-2 pt-3"
    />
  </div>

  <m-bean-operation
    v-for="(operation, name) in mBean.op"
    :key="`op-${name}`"
    :descriptor="operation"
    :name="name"
    @click="invoke(name, operation)"
  />
  <m-bean-operation-invocation
    v-if="invocation"
    :descriptor="invocation.descriptor"
    :name="invocation.name"
    :on-close="closeInvocation"
    :on-execute="execute"
  />
</template>

<script>
import Application from '@/services/application';
import Instance from '@/services/instance';
import { MBean } from '@/views/instances/jolokia/MBean';
import mBeanOperation from '@/views/instances/jolokia/m-bean-operation';
import mBeanOperationInvocation from '@/views/instances/jolokia/m-bean-operation-invocation';

export default {
  components: { mBeanOperation, mBeanOperationInvocation },
  props: {
    domain: {
      type: String,
      required: true,
    },
    mBean: {
      type: MBean,
      required: true,
    },
    application: {
      type: Application,
      required: true,
    },
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    invocation: null,
    scope: 'instance',
  }),
  methods: {
    closeInvocation() {
      this.invocation = null;
    },
    invoke(name, descriptor) {
      this.invocation = { name, descriptor };
    },
    execute(args) {
      const target =
        this.scope === 'instance' ? this.instance : this.application;
      return target.invokeMBeanOperation(
        this.domain,
        this.mBean.descriptor.raw,
        this.invocation.name,
        args,
      );
    },
  },
};
</script>
