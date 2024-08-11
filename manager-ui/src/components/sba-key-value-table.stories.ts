import SbaKeyValueTable from './sba-key-value-table.vue';

export default {
  component: SbaKeyValueTable,
  title: 'Components/Key-Value Table',
};

const Template = (args) => {
  return {
    components: { SbaKeyValueTable },
    setup() {
      return { args };
    },
    template: `<sba-key-value-table v-bind="args">
      <template #customContent="slotProps">
        {{slotProps}}
      </template>
      <template #customId="slotProps">
        {{slotProps}}
      </template>
    </sba-key-value-table>`,
  };
};

export const Default = {
  render: Template,

  args: {
    map: {
      key1: 'value 1',
      key2: 'value 2',
      key3: 'value 3',
    },
  },
};

export const UsingSlots = {
  render: Template,

  args: {
    map: {
      ...Default.args.map,
      slotProps: 'Special Value',
      'a key with sapces': {
        id: 'customId',
        value: 'Custom value',
      },
    },
  },
};
