import moment from 'moment';
import * as Vue from 'vue';
import { createApp, h, onBeforeMount, onBeforeUnmount, reactive } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import App from './App.vue';
import components from './components/index';
import {
  CUSTOM_ROUTES_ADDED_EVENT,
  createViewRegistry,
  useViewRegistry,
} from './composables/ViewRegistry.js';
import {
  createApplicationStore,
  useApplicationStore,
} from './composables/useApplicationStore.js';
import i18n from './i18n';
import SmModalPlugin from './plugins/modal';
import views from './views';

import eventBus from '@/services/bus';

const applicationStore = createApplicationStore();
const viewRegistry = createViewRegistry();

if (process.env.NODE_ENV === 'development') {
  globalThis.__VUE_OPTIONS_API__ = true;
  globalThis.__VUE_PROD_DEVTOOLS__ = true;
}

globalThis.Vue = Vue;
globalThis.SM.viewRegistry = useViewRegistry();
globalThis.SM.useApplicationStore = useApplicationStore;
globalThis.SM.useI18n = () => i18n.global;
globalThis.SM.use = ({ install }) => {
  install({
    viewRegistry: globalThis.SM.viewRegistry,
    applicationStore: globalThis.SM.useApplicationStore,
    i18n: i18n.global,
  });
};

moment.locale(navigator.language.split('-')[0]);

const installables = [...views];
installables.forEach((installable) => {
  installable.install({
    viewRegistry,
    applicationStore,
  });
});

const app = createApp({
  setup() {
    const router = useRouter();
    const route = useRoute();
    const { applications, applicationsInitialized, error } =
      useApplicationStore();
    const { t } = useI18n();

    onBeforeMount(() => {
      applicationStore.start();
    });

    onBeforeUnmount(() => {
      applicationStore.stop();
    });

    const routesAddedEventHandler = async () => {
      eventBus.off(CUSTOM_ROUTES_ADDED_EVENT, routesAddedEventHandler);
      await router.replace(route);
    };
    eventBus.on(CUSTOM_ROUTES_ADDED_EVENT, routesAddedEventHandler);

    return () =>
      h(
        App,
        reactive({
          applications,
          applicationsInitialized,
          error,
          t,
        }),
      );
  },
});

app.use(i18n);
app.use(components);
app.use(SmModalPlugin, { i18n });
app.use(viewRegistry.createRouter());
app.mount('#app');
