import { StartupActuatorEventTree } from '@/services/startup-activator-tree';
import { parse, toMilliseconds } from '@/utils/iso8601-duration';

const regex = new RegExp('([^=\\s]*)=\\[([^\\]]*)\\]', 'gi');

function mapDuration(duration) {
  if (typeof duration === 'string') {
    return toMilliseconds(parse(duration));
  } else if (!isNaN(Number.parseFloat(duration))) {
    return toMilliseconds(duration);
  } else {
    return -1;
  }
}

export const StartupActuatorService = {
  parseAsTree(data) {
    const events = data.timeline.events || [];
    const eventsForTree = events
      .sort((a, b) => a.startupStep.id - b.startupStep.id)
      .map((event) => {
        event.startupStep.parent = this.getById(
          events,
          event.startupStep.parentId,
        );
        event.startupStep.children = this.getByParentId(
          events,
          event.startupStep.id,
        );

        event.startupStep.tags = event.startupStep.tags.map(this.parseTag);
        event.duration = mapDuration(event.duration);

        event.startupStep.depth = 0;

        return event;
      })
      .map((event) => {
        let parent = event.startupStep.parent;
        while (parent !== null && parent !== undefined) {
          parent = parent.startupStep.parent;
          event.startupStep.depth++;
        }

        return event;
      });

    return new StartupActuatorEventTree(eventsForTree);
  },
  getById(events, id) {
    return (events || []).find((event) => event.startupStep.id === id);
  },
  getByParentId(events, id) {
    return (events || []).filter((event) => event.startupStep.parentId === id);
  },
  parseTag(param) {
    if (param.key === 'event') {
      const parsed = {};
      parsed['eventName'] = param.value.split(':')[0];

      const matcher = param.value.matchAll(regex);
      for (const match of matcher) {
        parsed[match[1]] = match[2].split(',').map((s) => s.trim());
      }
      param.parsed = parsed;
    }

    return param;
  },
};
