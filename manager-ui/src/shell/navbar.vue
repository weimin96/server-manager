

<template>
  <sba-navbar :brand="brand" class="text-sm lg:text-base">
    <sba-navbar-nav>
      <template v-for="item in topLevelViews" :key="item.id">
        <template v-if="item.children.length === 0">
          <sba-nav-item
            v-if="!item.href && item.name"
            :to="{ name: item.name }"
          >
            <component :is="item.handle" :error="error" />
          </sba-nav-item>
          <sba-nav-item
            v-else-if="item.href !== '#'"
            :href="item.href"
            target="blank"
          >
            <component :is="item.handle" :error="error" />
          </sba-nav-item>
          <sba-nav-item v-else>
            <component :is="item.handle" :error="error" />
          </sba-nav-item>
        </template>
        <template v-else>
          <sba-nav-dropdown :href="item.href">
            <template #label>
              <component :is="item.handle" />
            </template>
            <template #default>
              <sba-dropdown-item
                v-for="child in item.children"
                :key="child.name"
                :to="{ name: child.name }"
                v-bind="{ ...child }"
              >
                <component :is="child.handle" :error="error" />
              </sba-dropdown-item>
            </template>
          </sba-nav-dropdown>
        </template>
      </template>
    </sba-navbar-nav>
  </sba-navbar>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

import SbaDropdownItem from '@/components/sba-dropdown/sba-dropdown-item.vue';
import SbaNavDropdown from '@/components/sba-nav/sba-nav-dropdown.vue';
import SbaNavItem from '@/components/sba-nav/sba-nav-item.vue';
import SbaNavbarNav from '@/components/sba-navbar/sba-navbar-nav.vue';
import SbaNavbar from '@/components/sba-navbar/sba-navbar.vue';

import { useViewRegistry } from '@/composables/ViewRegistry';
import sbaConfig from '@/sba-config';
import { compareBy } from '@/utils/collections';

defineProps({
  error: {
    type: Error,
    default: null,
  },
});

const { views } = useViewRegistry();

const brand = sbaConfig.uiSettings.brand;

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
