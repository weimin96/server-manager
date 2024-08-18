<template>
  <sm-nav-dropdown data-testid="usermenu">
    <template #label>
      <font-awesome-icon
        class="w-10 rounded-full white mr-2"
        color="white"
        icon="user-circle"
      />
      <strong v-text="username" />
    </template>
    <sm-dropdown-item>
      Signed in as: <strong v-text="username" />
    </sm-dropdown-item>

    <template v-if="userSubMenuItems.length > 0">
      <sm-dropdown-divider />

      <template v-for="item in userSubMenuItems" :key="item.name">
        <sm-dropdown-item
          v-if="!item.href && item.name"
          :to="{ name: item.name }"
        >
          <component :is="item.handle" />
        </sm-dropdown-item>
        <sm-dropdown-item
          v-else-if="item.href !== '#'"
          :href="item.href"
          target="blank"
        >
          <component :is="item.handle" />
        </sm-dropdown-item>
        <sm-dropdown-item v-else>
          <component :is="item.handle" />
        </sm-dropdown-item>
      </template>
    </template>

    <sm-dropdown-divider />

    <sm-dropdown-logout-item />
  </sm-nav-dropdown>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

import SmDropdownDivider from '@/components/sm-dropdown/sm-dropdown-divider.vue';
import SmDropdownItem from '@/components/sm-dropdown/sm-dropdown-item.vue';
import SmNavDropdown from '@/components/sm-nav/sm-nav-dropdown.vue';

import { useViewRegistry } from '@/composables/ViewRegistry';
import { getCurrentUser } from '@/config';
import SmDropdownLogoutItem from '@/shell/sm-dropdown-logout-item.vue';

const currentUser = getCurrentUser();
const username = currentUser ? currentUser.name : null;

const { views } = useViewRegistry();

const userSubMenuItems = computed(() => {
  return views.filter((v) => v.parent === 'user');
});
</script>
