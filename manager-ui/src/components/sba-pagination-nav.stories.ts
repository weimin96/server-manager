import SbaPaginationNav from './sba-pagination-nav.vue';

import i18n from '@/i18n';

export default {
  component: SbaPaginationNav,
  title: 'Components/Pagination',
};

const Template = (args) => {
  return {
    components: { SbaPaginationNav },
    setup() {
      return { args };
    },
    methods: {
      change($event) {
        this.current = $event;
      },
    },
    data() {
      return {
        current: 1,
      };
    },
    template: `
      <sba-pagination-nav v-bind="args" @update="change"/>
    `,
    i18n,
  };
};

export const NoPages = {
  render: Template,

  args: {
    pageCount: 0,
    modelValue: 1,
  },
};

export const OnePage = {
  render: Template,

  args: {
    pageCount: 1,
  },
};

export const ManyPages = {
  render: Template,

  args: {
    modelValue: 1,
    pageCount: 12,
  },
};
