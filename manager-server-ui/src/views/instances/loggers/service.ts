import { flatMap, groupBy, union } from 'lodash-es';

const convertLoggers = function (loggers, instanceId) {
  return Object.entries(loggers).map(([name, config]) => ({
    name,
    level: [{ ...config, instanceId }],
  }));
};

export class InstanceLoggers {
  constructor(instance) {
    this.instance = instance;
  }

  async fetchLoggers() {
    const loggerConfig = (await this.instance.fetchLoggers()).data;
    return {
      errors: [],
      levels: loggerConfig.levels,
      loggers: convertLoggers(loggerConfig.loggers, this.instance.id),
    };
  }

  async configureLogger(name, level) {
    await this.instance.configureLogger(name, level);
  }
}

export class ApplicationLoggers {
  constructor(application) {
    this.application = application;
  }

  async fetchLoggers() {
    let errors;
    let levels;
    let loggers;

    try {
      const responses = (await this.application.fetchLoggers()).responses;
      const successful = responses.filter(
        (r) => r.body && r.status >= 200 && r.status < 299,
      );

      errors = responses
        .filter((r) => r.status >= 400)
        .map((r) => ({
          instanceId: r.instanceId,
          error: 'HTTP Status ' + r.status,
        }));
      loggers = Object.entries(
        groupBy(
          flatMap(successful, (r) =>
            convertLoggers(r.body.loggers, r.instanceId),
          ),
          (l) => l.name,
        ),
      ).map(([name, configs]) => ({
        name,
        level: flatMap(configs, (c) => c.level),
      }));
      levels = union(...successful.map((r) => r.body.levels));
    } catch {
      console.warn('Failed to fetch loggers for some instances');

      errors = [];
      levels = [];
      loggers = [];
    }

    return {
      errors,
      levels,
      loggers,
    };
  }

  async configureLogger(name, level) {
    let responses;

    try {
      responses = (await this.application.configureLogger(name, level))
        .responses;
    } catch {
      responses = [];
    }

    const errors = responses
      .filter((r) => r.status >= 400)
      .map((r) => ({
        instanceId: r.instanceId,
        error: 'HTTP Status ' + r.status,
      }));
    if (errors.length > 0) {
      console.warn('Failed to set loglevel for some instances', errors);
    }
  }
}
