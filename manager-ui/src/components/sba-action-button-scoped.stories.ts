import SbaActionButtonScoped from './sba-action-button-scoped.vue';

import i18n from '@/i18n';

export default {
  component: SbaActionButtonScoped,
  title: 'Components/Buttons/Action Button Scoped',
};

const TemplateWithProps = (args) => ({
  components: { SbaActionButtonScoped },
  setup() {
    return { args };
  },
  template: '<sba-action-button-scoped v-bind="args" />',
  i18n,
});

export const OneInstanceSuccessful = {
  render: TemplateWithProps,

  args: {
    instanceCount: 1,
    label: 'Push me!',
    actionFn() {
      return new Promise((resolve) => {
        setTimeout(() => resolve(), 2000);
      });
    },
  },
};

export const MultipleInstancesSuccessful = {
  render: TemplateWithProps,

  args: {
    ...OneInstanceSuccessful.args,
    instanceCount: 10,
  },
};

export const OneInstanceFailing = {
  render: TemplateWithProps,

  args: {
    ...OneInstanceSuccessful.args,
    instanceCount: 1,
    actionFn() {
      return new Promise((resolve, reject) => {
        setTimeout(() => {
          reject();
        }, 2000);
      });
    },
  },
};

const TemplateWithSlot = (args) => ({
  components: { SbaActionButtonScoped },
  setup() {
    return { args };
  },
  template: `
    <sba-action-button-scoped v-bind="args">
      <template v-slot="slotProps">
        <span v-if="slotProps.refreshStatus === 'executing'">Working...</span>
        <span v-else-if="slotProps.refreshStatus === 'completed'">Status Is Completed</span>
        <span v-else-if="slotProps.refreshStatus === 'failed'">Status Is Failed</span>
        <span v-else>Default Label</span>
      </template>
    </sba-action-button-scoped>`,
  i18n,
});

export const SlottedOneInstanceSuccessful = {
  render: TemplateWithSlot,

  args: {
    instanceCount: 1,
    actionFn() {
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve();
        }, 2000);
      });
    },
  },
};

export const SlottedOneInstanceFailing = {
  render: TemplateWithSlot,

  args: {
    instanceCount: 1,
    actionFn() {
      return new Promise((resolve, reject) => {
        setTimeout(() => {
          reject();
        }, 2000);
      });
    },
  },
};
