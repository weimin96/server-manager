import { useModal } from './api';

const SmModalPlugin = {
  install: (app, options = {}) => {
    const instance = useModal(options);
    app.config.globalProperties.$smModal = instance;
    app.provide('$smModal', instance);
  },
};

export default SmModalPlugin;
