import SbaButton from './sba-button.vue';

export default {
  component: SbaButton,
  title: 'Components/Buttons/Button',
};

const Template = (args) => {
  return {
    components: { SbaButton },
    setup() {
      return { args };
    },
    template: `<sba-button v-bind="args">${args.label}</sba-button>`,
  };
};

export const DefaultButton = {
  render: Template,

  args: {
    label: 'Default button',
  },
};

export const PrimaryButton = {
  render: Template,

  args: {
    label: 'Primary button',
    primary: 'primary',
  },
};

export const DangerButton = {
  render: Template,

  args: {
    label: 'Danger button',
    class: 'is-danger',
  },
};

export const SuccessButton = {
  render: Template,

  args: {
    label: 'Danger button',
    class: 'is-success',
  },
};

const SizeTemplate = () => {
  return {
    components: { SbaButton },
    template: `
      <sba-button size="xs">button xs</sba-button>
      <sba-button size="sm">button sm</sba-button>
      <sba-button size="base">button base</sba-button>
    `,
  };
};

export const ButtonSizes = {
  render: SizeTemplate,
};
