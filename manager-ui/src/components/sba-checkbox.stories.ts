import SbaCheckbox from './sba-checkbox.vue';

export default {
  component: SbaCheckbox,
  title: 'Components/Checkbox',
};

const Template = (args) => {
  return {
    components: { SbaCheckbox },
    setup() {
      return { args };
    },
    template: '<sba-checkbox v-bind="args" />{{args}}',
  };
};

export const OneButton = {
  render: Template,

  args: {
    modelValue: true,
    label: 'I am a label',
  },
};
