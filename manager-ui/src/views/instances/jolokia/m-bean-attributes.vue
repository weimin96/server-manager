

<template>
  <div>
    <div v-if="loading" class="mt-4 mb-2">
      <sba-loading-spinner />
    </div>
    <template v-else>
      <div
        v-if="application.instances.length > 1"
        class="absolute right-0 top-0"
      >
        <sba-toggle-scope-button
          v-model="scope"
          :instance-count="application.instances.length"
          class="bg-white px-4 py-2 pt-3"
        />
      </div>

      <sba-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />

      <m-bean-attribute
        v-for="(attribute, name) in mBean.attr"
        :key="`attr-${name}`"
        :descriptor="attribute"
        :name="name"
        :on-save-value="(value) => writeAttribute(name, value)"
        :value="attributeValues && attributeValues[name]"
      />
    </template>
  </div>
</template>

<script>
import Application from '@/services/application';
import Instance from '@/services/instance';
import { MBean } from '@/views/instances/jolokia/MBean';
import mBeanAttribute from '@/views/instances/jolokia/m-bean-attribute';

export default {
  components: { mBeanAttribute },
  props: {
    domain: {
      type: String,
      required: true,
    },
    mBean: {
      type: MBean,
      required: true,
    },
    instance: {
      type: Instance,
      required: true,
    },
    application: {
      type: Application,
      required: true,
    },
  },
  data: () => ({
    attributeValues: null,
    error: null,
    scope: 'instance',
    loading: true,
  }),
  created() {
    this.readAttributes();
  },
  methods: {
    async readAttributes() {
      try {
        this.loading = true;
        const response = await this.instance.readMBeanAttributes(
          this.domain,
          this.mBean.descriptor.raw,
        );
        this.attributeValues = response.data.value;
      } catch (error) {
        console.warn('Fetching MBean attributes failed:', error);
        this.error = error;
      } finally {
        this.loading = false;
      }
    },
    writeAttribute: async function (attribute, value) {
      try {
        const target =
          this.scope === 'instance' ? this.instance : this.application;
        return await target.writeMBeanAttribute(
          this.domain,
          this.mBean.descriptor.raw,
          attribute,
          value,
        );
      } catch (error) {
        console.warn(`Error saving attribute ${attribute}`, error);
      }
    },
  },
};
</script>
