<template>
  <sm-navbar :brand="brand" class="text-sm lg:text-base">
    <sm-navbar-nav>
      <template v-for="item in topLevelViews" :key="item.id">
        <template v-if="item.children.length === 0">
          <sm-nav-item v-if="!item.href && item.name" :to="{ name: item.name }">
            <component :is="item.handle" />
          </sm-nav-item>
          <sm-nav-item
            v-else-if="item.href !== '#'"
            :href="item.href"
            target="blank"
          >
            <component :is="item.handle" />
          </sm-nav-item>
          <sm-nav-item v-else>
            <component :is="item.handle" />
          </sm-nav-item>
        </template>
        <template v-else>
          <sm-nav-dropdown :href="item.href">
            <template #label>
              <component :is="item.handle" />
            </template>
            <template #default>
              <sm-dropdown-item
                v-for="child in item.children"
                :key="child.name"
                :to="{ name: child.name }"
                v-bind="{ ...child }"
              >
                <component :is="child.handle" />
              </sm-dropdown-item>
            </template>
          </sm-nav-dropdown>
        </template>
      </template>
    </sm-navbar-nav>
    <sm-navbar-nav class="ml-auto">
      <sm-nav-usermenu v-if="showUserMenu" />
    </sm-navbar-nav>
  </sm-navbar>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

import SmDropdownItem from '@/components/sm-dropdown/sm-dropdown-item.vue';
import SmNavDropdown from '@/components/sm-nav/sm-nav-dropdown.vue';
import SmNavItem from '@/components/sm-nav/sm-nav-item.vue';
import SmNavbarNav from '@/components/sm-navbar/sm-navbar-nav.vue';
import SmNavbar from '@/components/sm-navbar/sm-navbar.vue';

import { useViewRegistry } from '@/composables/ViewRegistry';
import SmConfig, { getCurrentUser } from '@/main/config';
import SmNavUsermenu from '@/shell/sm-nav-usermenu.vue';
import { compareBy } from '@/utils/collections';

defineProps({
  error: {
    type: Error,
    default: null,
  },
});

const { views } = useViewRegistry();

const currentUser = getCurrentUser();
const showUserMenu = !!currentUser && Object.hasOwn(currentUser, 'name');

const brand = SmConfig.uiSettings.brand;

const topLevelViews = computed(() => {
  let rootViews = views
    .filter((view) => {
      return (
        !view.parent &&
        !view.name?.includes('instance') &&
        !view.name?.includes('about') &&
        !view.path?.includes('/instance') &&
        view.isEnabled()
      );
    })
    .sort(compareBy((v) => v.order));

  return rootViews.map((rootView) => {
    const children = views.filter((v) => v.parent === rootView.name);

    return {
      ...rootView,
      children,
    };
  });
});
</script>
