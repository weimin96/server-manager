import SbaInput from './sba-input.vue';

export default {
  component: SbaInput,
  title: 'Components/Form/Input',
};

const Template = (args) => {
  return {
    components: { SbaInput },
    setup() {
      return { args };
    },
    template: `
      <sba-input v-bind="args">${args.slots}</sba-input>
    `,
  };
};

export const SimpleInput = {
  render: Template,

  args: {
    modelValue: 'Hello there!',
  },
};

export const InputWithError = {
  render: Template,

  args: {
    ...SimpleInput.args,
    error: 'Please provide a value.',
  },
};

export const InputWithHint = {
  render: Template,

  args: {
    ...SimpleInput.args,
    hint: 'This is a hint',
  },
};

export const InputWithPrepend = {
  render: Template,

  args: {
    ...SimpleInput.args,
    slots: `
      <template #prepend>Prepend</template>
    `,
  },
};

export const InputWithAppend = {
  render: Template,

  args: {
    ...SimpleInput.args,
    inputClass: 'text-right',
    slots: `
      <template #append>EUR</template>
    `,
  },
};

export const Complex = {
  render: Template,

  args: {
    ...SimpleInput.args,
    label: 'Label',
    error: 'Please provide a value.',
    hint: 'I am a hint!',
  },
};
