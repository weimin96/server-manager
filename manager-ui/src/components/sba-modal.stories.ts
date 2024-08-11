import SbaModal from './sba-modal.vue';

import i18n from '@/i18n';

export default {
  component: SbaModal,
  title: 'Components/Modal',
};

const Template = (args) => ({
  components: { SbaModal },
  setup() {
    return {
      args,
    };
  },
  template: `
    <sba-modal v-bind="args">
      <template v-if="${'header' in args}" #header>${args.header}</template>
      <template v-if="${'body' in args}" #body>${args.body}</template>
      <template v-if="${'footer' in args}" #footer>${args.footer}</template>
    </sba-modal>
  `,
  i18n,
});

export const ModalWithBody = {
  render: Template,

  args: {
    modelValue: true,
    body: 'I am a body',
  },
};

export const ModalWithHeaderAndFooter = {
  render: Template,

  args: {
    modelValue: true,
    header: 'You are awesome!',
    body: '<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla accumsan, metus ultrices eleifend gravida, nulla nunc varius lectus, nec rutrum justo nibh eu lectus.</p>',
    footer: '<sba-button class="button">Close me!</sba-button>',
  },
};
