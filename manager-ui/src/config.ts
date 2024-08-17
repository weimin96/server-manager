import { merge } from 'lodash-es';

const brand = '<img src="assets/img/icon-server-manager.svg">Server Manager';

const DEFAULT_CONFIG = {
  uiSettings: {
    brand,
    theme: {
      backgroundEnabled: true,
      color: '#42d3a5',
    },
    rememberMeEnabled: true,
    externalViews: [] as ExternalView[],
    favicon: 'assets/img/favicon.png',
    faviconDanger: 'assets/img/favicon-danger.png',
    routes: [],
    viewSettings: [],
    pollTimer: {
      cache: 2500,
      datasource: 2500,
      gc: 2500,
      process: 2500,
      memory: 2500,
      threads: 2500,
      logfile: 1000,
    },
  },
  user: null,
  csrf: {
    parameterName: '_csrf',
    headerName: 'X-XSRF-TOKEN',
  },
};

const mergedConfig = merge(DEFAULT_CONFIG, window.SM);

export const getCurrentUser = () => {
  return mergedConfig.user;
};

export default mergedConfig;
