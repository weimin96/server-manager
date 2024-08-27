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

    <sm-dropdown-logout-item />
  </sm-nav-dropdown>
</template>

<script lang="ts" setup>

import SmNavDropdown from '@/components/sm-nav/sm-nav-dropdown.vue';

import { getCurrentUser } from '@/config';
import SmDropdownLogoutItem from '@/shell/sm-dropdown-logout-item.vue';

const currentUser = getCurrentUser();
const username = currentUser ? currentUser.name : null;

</script>
