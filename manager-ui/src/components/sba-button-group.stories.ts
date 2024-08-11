import SbaButtonGroup from './sba-button-group.vue';
import SbaButton from './sba-button.vue';

export default {
  component: SbaButtonGroup,
  title: 'Components/Buttons/Button Group',
};

const Template = (args) => {
  return {
    components: { SbaButtonGroup, SbaButton },
    setup() {
      return { args };
    },
    template: `<sba-button-group v-bind="args">${args.slot}</sba-button-group>`,
  };
};

export const OneButton = {
  render: Template,

  args: {
    slot: '<sba-button>First</sba-button>',
  },
};

export const TwoButtons = {
  render: Template,

  args: {
    slot: `
      <sba-button>First</sba-button>
      <sba-button>Second</sba-button>
    `,
  },
};

export const MoreButtons = {
  render: Template,

  args: {
    slot: `
      <sba-button>First</sba-button>
      <sba-button>Second</sba-button>
      <sba-button>Third</sba-button>
    `,
  },
};
