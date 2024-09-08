import { merge } from 'lodash-es';

const brand =
  '<img style="width: auto;height: 45px" src="../assets/img/banner.svg">';

const DEFAULT_CONFIG = {
  uiSettings: {
    brand,
    externalViews: [] as ExternalView[],
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
  csrf: {
    parameterName: '_csrf',
    headerName: 'X-XSRF-TOKEN',
  },
};

const mergedConfig = merge(DEFAULT_CONFIG, window.SM);

export const getCurrentUser = () => {
  const username = localStorage.getItem('currentUser');
  return { name: username };
};

export const setCurrentUser = (username: string) => {
  localStorage.setItem('currentUser', username);
};

export default mergedConfig;
