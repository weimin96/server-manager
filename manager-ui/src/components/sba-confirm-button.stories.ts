import SbaConfirmButton from './sba-confirm-button.vue';

export default {
  component: SbaConfirmButton,
  title: 'Components/Buttons/Confirm Button',
};

const Template = (args) => {
  return {
    components: { SbaConfirmButton },
    setup() {
      return { args };
    },
    template: `<sba-confirm-button v-bind="args">${args.label}</sba-confirm-button>`,
  };
};

export const DefaultButton = {
  render: Template,

  args: {
    label: 'Default confirm button',
  },
};
