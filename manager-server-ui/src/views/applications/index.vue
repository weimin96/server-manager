<template>
  <div>
    <ApplicationStats />
    <div
      v-if="applications.length > 0"
      class="mx-16 rounded-xl border shadow bg-white md:mt-0 mt-16"
    >
      <div class="flex flex-col gap-y-1.5 p-6">
        <h3 class="font-semibold leading-none tracking-tight">概览</h3>
      </div>
      <sm-panel v-if="!applicationsInitialized">
        <p class="is-muted is-loading" v-text="'应用加载中...'" />
      </sm-panel>

      <template v-if="applicationsInitialized">
        <application-item
          v-for="group in grouped"
          :id="group.name"
          :key="group.name"
          v-on-clickaway="(event: Event) => deselect(event, group.name)"
          :seamless="true"
          :title="group.name"
          :subtitle="`${group.instances?.length ?? 0}个实例`"
          class="application-group"
          :aria-expanded="isExpanded(group.name)"
          @title-click="
            () => {
              select(group.name);
              toggleGroup(group.name);
            }
          "
        >
          <template #prefix>
            <font-awesome-icon
              icon="chevron-down"
              :class="{
                '-rotate-90': !isExpanded(group.name),
                'mr-2 transition-[transform]': true,
              }"
            />
            <sm-status-badge
              v-if="isGroupedByApplication"
              class="ml-1 mr-2"
              :status="
                applicationStore.findApplicationByInstanceId(
                  group.instances[0].id,
                )?.status
              "
            />
          </template>

          <template v-if="isGroupedByApplication" #actions>
            <ApplicationListItemAction
              :item="
                applicationStore.findApplicationByInstanceId(
                  group.instances[0].id,
                )
              "
              @filter-settings="toggleNotificationFilterSettings"
            />
          </template>

          <template v-if="isExpanded(group.name)" #default>
            <InstancesList :instances="group.instances">
              <template #actions="{ instance }">
                <ApplicationListItemAction
                  :item="instance"
                  class="md:hidden"
                  @filter-settings="toggleNotificationFilterSettings"
                />
              </template>
            </InstancesList>
          </template>
        </application-item>

        <NotificationFilterSettings
          v-if="showNotificationFilterSettingsObject"
          v-on-clickaway="() => toggleNotificationFilterSettings(null)"
          v-popper="
            `nf-settings-${
              showNotificationFilterSettingsObject.id ||
              showNotificationFilterSettingsObject.name
            }`
          "
          :notification-filters="notificationFilters"
          :object="showNotificationFilterSettingsObject"
          @filter-add="addFilter"
          @filter-remove="removeFilter"
        />
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { groupBy, sortBy, transform } from 'lodash-es';
import { computed, nextTick, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useApplicationStore } from '@/composables/useApplicationStore';
import Application from '@/services/application';
import Instance from '@/services/instance';
import NotificationFilter from '@/services/notification-filter';
import { anyValueMatches } from '@/utils/collections';
import { concatMap, mergeWith, Subject, timer } from '@/utils/rxjs';
import { useRouterState } from '@/utils/useRouterState';
import { useSubscription } from '@/utils/useSubscription';
import ApplicationListItemAction from '@/views/applications/ApplicationListItemAction.vue';
import ApplicationStats from '@/views/applications/ApplicationStats.vue';
import InstancesList from '@/views/applications/InstancesList.vue';
import NotificationFilterSettings from '@/views/applications/NotificationFilterSettings.vue';
import ApplicationItem from '@/views/applications/ApplicationItem.vue';
import SmStatusBadge from '@/components/sm-status-badge.vue';

const props = defineProps({
  selected: {
    type: String,
    default: null,
  },
});

const instanceMatchesFilter = (term: string, instance: Instance) => {
  const predicate = (value: string | number) =>
    String(value).toLowerCase().includes(term);

  return (
    anyValueMatches(instance.registration, predicate) ||
    anyValueMatches(instance.buildVersion, predicate) ||
    anyValueMatches(instance.id, predicate) ||
    anyValueMatches(instance.tags, predicate)
  );
};

type NotificationFilterSettingsObject = { id: string; name: string };

type InstancesListType = {
  name?: string;
  statusKey?: string;
  status?: string;
  instances?: Instance[];
  applications?: Application[];
};

const groupingFunctions = {
  application: (instance: Instance) => instance.registration.name,
  group: (instance: Instance) =>
    instance.registration.metadata?.['group'] ?? 'term.no_group',
};

const router = useRouter();
const route = useRoute();
const { applications, applicationsInitialized, applicationStore } =
  useApplicationStore();
const expandedGroups = ref([props.selected]);
const groupingFunction = ref(groupingFunctions.application);

const routerState = useRouterState({
  q: '',
});
const notificationFilterSubject = new Subject();
const notificationFilters = ref([]);

useSubscription(
  timer(0, 60000)
    .pipe(
      mergeWith(notificationFilterSubject),
      concatMap(fetchNotificationFilters),
    )
    .subscribe({
      next: (data) => {
        notificationFilters.value = data;
      },
      error: (error) => {
        console.warn('Fetching notification filters failed with error:', error);
      },
    }),
);

async function fetchNotificationFilters() {
  return [];
}

const grouped = computed(() => {
  const filteredApplications = filterInstances(applications.value);

  const instances = filteredApplications.flatMap(
    (application: Application) => application.instances,
  );

  const grouped = groupBy<Instance>(instances, groupingFunction.value);

  const list = transform<Instance[], InstancesListType[]>(
    grouped,
    (result, instances, name) => {
      result.push({
        name,
        instances: sortBy(instances, [
          (instance) => instance.registration.name,
        ]),
      });
    },
    [],
  );

  return sortBy(list, [(item) => getApplicationStatus(item)]);
});

function getApplicationStatus(item: InstancesListType): string {
  return applicationStore.findApplicationByInstanceId(item.instances[0].id)
    ?.status;
}

function filterInstances(applications: Application[]) {
  if (!routerState.q) {
    return applications;
  }

  return applications
    .map((application) =>
      application.filterInstances((i) =>
        instanceMatchesFilter(routerState.q.toLowerCase(), i),
      ),
    )
    .filter((application) => application.instances.length > 0);
}

const isGroupedByApplication = computed(() => {
  return groupingFunction.value === groupingFunctions.application;
});

if (props.selected) {
  scrollIntoView(props.selected);
}

async function scrollIntoView(id) {
  if (id) {
    await nextTick();
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({
        behavior: 'smooth',
        block: 'end',
        inline: 'nearest',
      });
    }
  }
}

const showNotificationFilterSettingsObject = ref(
  null as unknown as NotificationFilterSettingsObject,
);

function isExpanded(name: string) {
  return expandedGroups.value.includes(name);
}

function toggleGroup(name: string) {
  if (expandedGroups.value.includes(name)) {
    expandedGroups.value = expandedGroups.value.filter((n) => n !== name);
  } else {
    expandedGroups.value = [...expandedGroups.value, name];
  }
}

function select(name: string) {
  router.push({
    name: 'applications',
    params: { selected: name },
    query: { ...route.query },
  });
}

function deselect(event: Event, expectedSelected: string) {
  if (event && event.target instanceof HTMLAnchorElement) {
    return;
  }
  toggleNotificationFilterSettings(null);
  if (!expectedSelected || props.selected === expectedSelected) {
    router.push({ name: 'applications' });
  }
}

async function addFilter({ object, ttl }) {
  try {
    const response = await NotificationFilter.addFilter(object, ttl);
    let notificationFilter = response.data;
    notificationFilterSubject.next(notificationFilter);
  } catch (error) {
    console.warn('Adding notification filter failed:', error);
  } finally {
    toggleNotificationFilterSettings(null);
  }
}

async function removeFilter(activeFilter) {
  try {
    await activeFilter.delete();
    notificationFilterSubject.next(activeFilter.id);
  } catch (error) {
    console.warn('Deleting notification filter failed:', error);
  } finally {
    toggleNotificationFilterSettings(null);
  }
}

function toggleNotificationFilterSettings(obj) {
  showNotificationFilterSettingsObject.value = obj ? obj : null;
}
</script>

<script lang="ts">
import { defineComponent } from 'vue';
import { directive as onClickaway } from 'vue3-click-away';

import Popper from '@/directives/popper';
import handle from '@/views/applications/handle.vue';

export default defineComponent({
  directives: { Popper, onClickaway },
  install({ viewRegistry }) {
    viewRegistry.addView({
      path: '/applications/:selected?',
      props: true,
      name: 'applications',
      handle,
      order: 0,
      component: this,
    });
    viewRegistry.addRedirect('/', 'applications');
  },
});
</script>
