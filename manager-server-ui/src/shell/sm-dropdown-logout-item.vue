<template>
  <form action="logout" class="w-full" method="post">
    <sm-dropdown-item as="button" type="submit">
      <input
        v-if="csrfToken"
        :name="csrfParameterName"
        :value="csrfToken"
        type="hidden"
      />
      <font-awesome-icon icon="sign-out-alt" />&nbsp;<span
        v-text="$t('navbar.logout')"
      />
    </sm-dropdown-item>
  </form>
</template>

<script lang="ts" setup>
import SmDropdownItem from '@/components/sm-dropdown/sm-dropdown-item';

import SmConfig from '@/config';

const readCookie = (name) => {
  const match = document.cookie.match(
    new RegExp('(^|;\\s*)(' + name + ')=([^;]*)'),
  );
  return match ? decodeURIComponent(match[3]) : null;
};

const csrfToken = readCookie('XSRF-TOKEN');
const csrfParameterName = SmConfig.csrf.parameterName;
</script>
