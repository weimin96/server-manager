import SbaAlert, { Severity } from './sba-alert.vue';

export default {
  component: SbaAlert,
  title: 'Components/Alert',
};

const Template = (args) => ({
  components: { SbaAlert },
  setup() {
    return { args };
  },
  template: '<sba-alert v-bind="args" />',
});

export const AlertError = {
  render: Template,

  args: {
    title: 'Server error',
    error: new Error('Error reading from endpoint /applications'),
    severity: Severity.ERROR,
  },
};

export const AlertErrorWithoutTitle = {
  render: Template,

  args: {
    error: new Error('Error reading from endpoint /applications'),
    severity: Severity.ERROR,
  },
};

export const AlertWarning = {
  render: Template,

  args: {
    ...AlertError.args,
    title: 'Warning',
    error: new Error('The response took longer than expected.'),
    severity: 'WARN',
  },
};

export const AlertInfo = {
  render: Template,

  args: {
    ...AlertError.args,
    title: 'Hint',
    error: new Error('Check GC information as well!'),
    severity: 'INFO',
  },
};

export const AlertSuccess = {
  render: Template,

  args: {
    ...AlertError.args,
    title: 'Successful',
    error: new Error('Changes have been applied.'),
    severity: 'SUCCESS',
  },
};
