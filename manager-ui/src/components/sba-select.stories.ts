import SbaSelect from './sba-select.vue';

export default {
  component: SbaSelect,
  title: 'Components/Form/Select',
};

const Template = (args) => {
  return {
    components: { SbaSelect },
    setup() {
      return { args };
    },
    template: `
      <sba-select v-bind="args">
        <template #prepend>Prepend</template>
        <template #append>Append</template>
      </sba-select>
    `,
  };
};

export const SimpleSelect = {
  render: Template,

  args: {
    modelValue: 'berlin',
    options: [
      { value: 'beer', label: 'Beer' },
      { value: 'water', label: 'Water' },
      { value: 'wine', label: 'Wine' },
    ],
  },
};
