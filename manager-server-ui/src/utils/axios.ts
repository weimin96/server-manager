import axios from 'axios';

import SmConfig from '../config';

axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
axios.defaults.xsrfHeaderName = SmConfig.csrf.headerName;

export const redirectOn401 =
  (predicate: (error: any) => boolean = () => true) =>
  (error: any) => {
    if (error.response && error.response.status === 401 && predicate(error)) {
      window.location.assign(
        `login?redirectTo=${encodeURIComponent(
          window.location.href,
        )}&error=401`,
      );
    }
    return Promise.reject(error);
  };

axios.defaults.withCredentials = true;
axios.defaults.headers.common['Accept'] = 'application/json';
axios.interceptors.response.use((response) => response, redirectOn401());

export default axios;
