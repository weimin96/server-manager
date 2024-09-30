import { AxiosInstance } from 'axios';
import saveAs from 'file-saver';
import { Observable, concat, from, ignoreElements } from 'rxjs';

import axios, { redirectOn401 } from '../utils/axios.js';
import waitForPolyfill from '../utils/eventsource-polyfill';
import logtail from '../utils/logtail';
import uri from '../utils/uri';

const actuatorMimeTypes = [
  'application/vnd.spring-boot.actuator.v2+json',
  'application/vnd.spring-boot.actuator.v1+json',
  'application/json',
].join(',');

const isInstanceActuatorRequest = (url: string) =>
  url.match(/^instances[/][^/]+[/]monitor([/].*)?$/);

class Instance {
  public readonly id: string;
  public registration: Registration;
  public endpoints: any[] = [];
  public tags: { [key: string]: string }[];
  public statusTimestamp: string;
  public buildVersion: string;
  public statusInfo: StatusInfo;
  private readonly axios: AxiosInstance;

  constructor({ id, ...instance }) {
    Object.assign(this, instance);
    this.id = id.value;
    this.tags = instance.tags.values;
    this.axios = axios.create({
      withCredentials: true,
      baseURL: uri`api/instances/${this.id}`,
      headers: { Accept: actuatorMimeTypes },
    });
    this.axios.interceptors.response.use(
      (response) => response,
      redirectOn401(
        (error) =>
          !isInstanceActuatorRequest(error.config.baseURL + error.config.url),
      ),
    );
  }

  get isUnregisterable() {
    return this.registration.source === 'http-api';
  }

  static async fetchEvents() {
    return axios.get(uri`api/instances/events`, {
      headers: { Accept: 'application/json' },
    });
  }

  static getEventStream() {
    return concat(
      from(waitForPolyfill()).pipe(ignoreElements()),
      Observable.create((observer) => {
        const eventSource = new EventSource('api/instances/events');
        eventSource.onmessage = (message) =>
          observer.next({
            ...message,
            data: JSON.parse(message.data),
          });
        eventSource.onerror = (err) => observer.error(err);
        return () => {
          eventSource.close();
        };
      }),
    );
  }

  static async get(id) {
    return axios.get(uri`api/instances/${id}`, {
      headers: { Accept: 'application/json' },
      transformResponse(data) {
        if (!data) {
          return data;
        }
        const instance = JSON.parse(data);
        return new Instance(instance);
      },
    });
  }

  static _toMBeans(data) {
    if (!data) {
      return data;
    }
    const raw = JSON.parse(data);
    return Object.entries(raw.value).map(([domain, mBeans]) => ({
      domain,
      mBeans: Object.entries(mBeans).map(([descriptor, mBean]) => ({
        descriptor: descriptor,
        ...mBean,
      })),
    }));
  }

  getId() {
    return this.id;
  }

  hasEndpoint(endpointId) {
    return this.endpoints.some((endpoint) => endpoint.id === endpointId);
  }

  async unregister() {
    return this.axios.delete('', {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchInfo() {
    return this.axios.get(uri`monitor/info`);
  }

  async fetchMetrics() {
    return this.axios.get(uri`monitor/metrics`);
  }

  async fetchMetric(metric, tags) {
    const params = new URLSearchParams();
    if (tags) {
      let firstElementDuplicated = false;
      Object.entries(tags)
        .filter(([, value]) => typeof value !== 'undefined' && value !== null)
        .forEach(([name, value]) => {
          params.append('tag', `${name}:${value}`);

          if (!firstElementDuplicated) {
            params.append('tag', `${name}:${value}`);
            firstElementDuplicated = true;
          }
        });
    }
    return this.axios.get(uri`monitor/metrics/${metric}`, {
      params,
    });
  }

  async fetchHealth() {
    return await this.axios.get(uri`monitor/health`, { validateStatus: null });
  }

  async fetchHealthGroup(groupName: string) {
    return await this.axios.get(uri`monitor/health/${groupName}`, {
      validateStatus: null,
    });
  }

  async fetchEnv(name) {
    return this.axios.get(uri`monitor/env/${name || ''}`);
  }

  async fetchConfigprops() {
    return this.axios.get(uri`monitor/configprops`);
  }

  async hasEnvManagerSupport() {
    const response = await this.axios.options(uri`monitor/env`);
    return (
      response.headers['allow'] && response.headers['allow'].includes('POST')
    );
  }

  async resetEnv() {
    return this.axios.delete(uri`monitor/env`);
  }

  async setEnv(name, value) {
    return this.axios.post(
      uri`monitor/env`,
      { name, value },
      {
        headers: { 'Content-Type': 'application/json' },
      },
    );
  }

  async refreshContext() {
    return this.axios.post(uri`monitor/refresh`);
  }

  async fetchLiquibase() {
    return this.axios.get(uri`monitor/liquibase`);
  }

  async fetchScheduledTasks() {
    return this.axios.get(uri`monitor/scheduledtasks`);
  }

  async fetchGatewayGlobalFilters() {
    return this.axios.get(uri`monitor/gateway/globalfilters`);
  }

  async addGatewayRoute(route) {
    return this.axios.post(uri`monitor/gateway/routes/${route.id}`, route, {
      headers: { 'Content-Type': 'application/json' },
    });
  }

  async fetchGatewayRoutes() {
    return this.axios.get(uri`monitor/gateway/routes`);
  }

  async deleteGatewayRoute(routeId) {
    return this.axios.delete(uri`monitor/gateway/routes/${routeId}`);
  }

  async refreshGatewayRoutesCache() {
    return this.axios.post(uri`monitor/gateway/refresh`);
  }

  async fetchCaches() {
    return this.axios.get(uri`monitor/caches`);
  }

  async clearCaches() {
    return this.axios.delete(uri`monitor/caches`);
  }

  async clearCache(name, cacheManager) {
    return this.axios.delete(uri`monitor/caches/${name}`, {
      params: { cacheManager: cacheManager },
    });
  }

  async fetchLoggers() {
    return this.axios.get(uri`monitor/loggers`);
  }

  async configureLogger(name, level) {
    await this.axios.post(
      uri`monitor/loggers/${name}`,
      level === null ? {} : { configuredLevel: level },
      {
        headers: { 'Content-Type': 'application/json' },
      },
    );
  }

  async fetchHttptrace() {
    return this.axios.get(uri`monitor/httptrace`);
  }

  async fetchHttpExchanges() {
    return this.axios.get(uri`monitor/httpexchanges`);
  }

  async fetchBeans() {
    return this.axios.get(uri`monitor/beans`);
  }

  async fetchConditions() {
    return this.axios.get(uri`monitor/conditions`);
  }

  async fetchThreaddump() {
    return this.axios.get(uri`monitor/threaddump`);
  }

  async downloadThreaddump() {
    const res = await this.axios.get(uri`monitor/threaddump`, {
      headers: { Accept: 'text/plain' },
    });
    const blob = new Blob([res.data], { type: 'text/plain;charset=utf-8' });
    saveAs(blob, this.registration.name + '-threaddump.txt');
  }

  async fetchAuditevents({ after, type, principal }) {
    return this.axios.get(uri`monitor/auditevents`, {
      params: {
        after: after.toISOString(),
        type: type || undefined,
        principal: principal || undefined,
      },
    });
  }

  async fetchSessionsByUsername(username) {
    return this.axios.get(uri`monitor/sessions`, {
      params: {
        username: username || undefined,
      },
    });
  }

  async fetchSession(sessionId) {
    return this.axios.get(uri`monitor/sessions/${sessionId}`);
  }

  async deleteSession(sessionId) {
    return this.axios.delete(uri`monitor/sessions/${sessionId}`);
  }

  async fetchStartup() {
    const optionsResponse = await this.axios.options(uri`monitor/startup`);
    if (
      optionsResponse.headers.allow &&
      optionsResponse.headers.allow.includes('GET')
    ) {
      return this.axios.get(uri`monitor/startup`);
    }

    return this.axios.post(uri`monitor/startup`);
  }

  streamLogfile(interval) {
    return logtail(
      (opt) => this.axios.get(uri`monitor/logfile`, opt),
      interval,
    );
  }

  async logdir() {
    return this.axios.get(uri`monitor/logdir`, {
      headers: { Accept: 'application/json' },
    });
  }

  async downloadFile(url, filename) {
    return this.axios
      .get(url, {
        responseType: 'text',
        headers: {
          Accept: '*/*',
        },
      })
      .then((response) => {
        if (!response.status === 200) {
          ElMessage.erroe('下载失败');
        }
        return response.data;
      })
      .then((text) => {
        // 创建一个Blob对象并指定类型
        const blob = new Blob([text], { type: 'text/plain' });
        // 创建一个链接对象
        const link = document.createElement('a');
        // 创建一个指向Blob对象的URL
        link.href = URL.createObjectURL(blob);
        // 设置下载的文件名
        link.download = filename;
        // 模拟点击下载链接
        link.click();
        // 释放Blob对象的URL
        URL.revokeObjectURL(link.href);
      })
      .catch((error) => {
        console.error(
          'There has been a problem with your fetch operation:',
          error,
        );
      });
  }

  async druid(current, pageSize) {
    return this.axios.get(
      uri`monitor/druid?current=${current}&pageSize=${pageSize}`,
      {
        headers: { Accept: 'application/json' },
      },
    );
  }

  async logContent(fileName) {
    return this.axios.get(uri`monitor/logcontent/${fileName}`, {
      headers: { Accept: '*/*' },
    });
  }

  async listMBeans() {
    return this.axios.get(uri`monitor/jolokia/list`, {
      headers: { Accept: 'application/json' },
      params: { canonicalNaming: false },
      transformResponse: Instance._toMBeans,
    });
  }

  async readMBeanAttributes(domain, mBean) {
    const body = {
      type: 'read',
      mbean: `${domain}:${mBean}`,
      config: { ignoreErrors: true },
    };
    return this.axios.post(uri`monitor/jolokia`, body, {
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    });
  }

  async writeMBeanAttribute(domain, mBean, attribute, value) {
    const body = {
      type: 'write',
      mbean: `${domain}:${mBean}`,
      attribute,
      value,
    };
    return this.axios.post(uri`monitor/jolokia`, body, {
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    });
  }

  async invokeMBeanOperation(domain, mBean, operation, args) {
    const body = {
      type: 'exec',
      mbean: `${domain}:${mBean}`,
      operation,
      arguments: args,
    };
    return this.axios.post(uri`monitor/jolokia`, body, {
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    });
  }

  async fetchMappings() {
    return this.axios.get(uri`monitor/mappings`);
  }

  async fetchQuartzJobs() {
    return this.axios.get(uri`monitor/quartz/jobs`, {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchQuartzJob(group, name) {
    return this.axios.get(uri`monitor/quartz/jobs/${group}/${name}`, {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchQuartzTriggers() {
    return this.axios.get(uri`monitor/quartz/triggers`, {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchQuartzTrigger(group, name) {
    return this.axios.get(uri`monitor/quartz/triggers/${group}/${name}`, {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchSbomIds() {
    return this.axios.get(uri`monitor/sbom`, {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchSbom(id) {
    return this.axios.get(uri`monitor/sbom/${id}`, {
      headers: { Accept: '*/*' },
    });
  }

  shutdown() {
    return this.axios.post(uri`monitor/shutdown`);
  }

  restart() {
    return this.axios.post(uri`monitor/restart`);
  }
}

export default Instance;

export type Registration = {
  name: string;
  managementUrl?: string;
  healthUrl: string;
  serviceUrl?: string;
  source: string;
  metadata?: { [key: string]: string }[];
};

type StatusInfo = {
  status:
    | 'UNKNOWN'
    | 'OUT_OF_SERVICE'
    | 'UP'
    | 'DOWN'
    | 'OFFLINE'
    | 'RESTRICTED'
    | string;
  details: { [key: string]: string };
};

export const DOWN_STATES = ['OUT_OF_SERVICE', 'DOWN', 'OFFLINE', 'RESTRICTED'];
export const UP_STATES = ['UP'];
export const UNKNOWN_STATES = ['UNKNOWN'];
