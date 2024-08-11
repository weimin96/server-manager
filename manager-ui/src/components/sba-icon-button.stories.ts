import SbaIconButton from './sba-icon-button.vue';

export default {
  component: SbaIconButton,
  title: 'Components/Buttons/Icon Button',
};

const Template = (args) => {
  return {
    components: { SbaIconButton },
    setup() {
      return { args };
    },
    template: `
      <sba-icon-button v-bind="args">${args.label}</sba-icon-button>`,
  };
};

export const DefaultButton = {
  render: Template,

  args: {
    icon: 'trash',
    title: 'unregister',
  },
};
