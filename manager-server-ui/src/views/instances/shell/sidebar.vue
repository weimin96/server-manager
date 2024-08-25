<template>
  <aside
    :class="{ 'w-60': sidebarOpen }"
    class="h-[calc(100vh-100px)] w-72 shadow-sm border border-gray-100 rounded-xl flex flex-col bg-white border-r backdrop-filter backdrop-blur-lg bg-opacity-80 z-40 transition-all ml-4 my-4 fixed"
  >
    <ul class="relative px-1 py-1 overflow-y-auto">
      <!-- 头部信息 -->
      <li class="relative mb-1 hidden md:block">
        <router-link
          :class="`text-center instance-summary--${instance.statusInfo.status}`"
          :to="{
            name: 'instances/details',
            params: { instanceId: instance.id },
          }"
          class="instance-info-block"
        >
          <span class="overflow-hidden text-ellipsis">
            <h6
              class="block antialiased tracking-normal font-sans text-base font-semibold leading-relaxed text-gray-900"
              v-text="instance.registration.name"
            />
            <small><em v-text="instance.id" /></small>
          </span>
        </router-link>
      </li>

      <!-- 导航菜单按钮 -->
      <li class="block md:hidden">
        <a class="navbar-link navbar-link__group" @click.stop="toggleSidebar">
          <font-awesome-icon :icon="['fas', 'bars']" />
        </a>
      </li>

      <!-- 菜单列表 -->
      <li
        v-for="group in enabledGroupedViews"
        :key="group.name"
        :data-sm-group="group.id"
        class="relative"
      >
        <router-link
          :class="{ 'navbar-link__group__active': isActiveGroup(group) }"
          :to="{
            name: group.views[0].name,
            params: { instanceId: instance.id },
          }"
          active-class=""
          class="navbar-link navbar-link__group"
          exact-active-class=""
        >
          <span v-html="group.icon" />
          <span
            class="navbar-text"
            v-text="
              hasMultipleViews(group)
                ? getGroupTitle(group.id)
                : $t(group.views[0].label)
            "
          />
          <svg
            v-if="hasMultipleViews(group)"
            :class="{
              '-rotate-90': !isActiveGroup(group),
              '': isActiveGroup(group),
            }"
            aria-hidden="true"
            class="h-3 ml-auto hidden md:block text-blue-500"
            data-prefix="fas"
            focusable="false"
            role="img"
            viewBox="0 0 448 512"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z"
              fill="currentColor"
            />
          </svg>
        </router-link>

        <!-- Le subnav -->
        <ul
          v-if="hasMultipleViews(group) && isActiveGroup(group)"
          :class="{ 'hidden md:block': !sidebarOpen }"
          class="relative block"
        >
          <li
            v-for="view in group.views"
            :key="view.name"
            :data-sm-view="view.name"
          >
            <router-link
              :to="{ name: view.name, params: { instanceId: instance.id } }"
              active-class="navbar-link__active"
              class="navbar-link navbar-link__group_item"
              exact-active-class=""
            >
              <component :is="view.handle" />
            </router-link>
          </li>
        </ul>
      </li>
    </ul>
  </aside>
</template>

<script lang="ts">
import { defineComponent, toRaw } from 'vue';

import SmButton from '@/components/sm-button.vue';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { compareBy } from '@/utils/collections';
import { VIEW_GROUP_ICON } from '@/views/ViewGroup';

export default defineComponent({
  components: { SmButton },
  props: {
    views: {
      type: Array,
      default: () => [],
    },
    instance: {
      type: Instance,
      default: null,
    },
    application: {
      type: Application,
      default: null,
    },
  },
  data() {
    return {
      sidebarOpen: false,
    };
  },
  computed: {
    enabledViews() {
      if (!this.instance) {
        return [];
      }

      return [...this.views]
        .filter(
          (view) =>
            typeof view.isEnabled === 'undefined' ||
            view.isEnabled({ instance: this.instance }),
        )
        .sort(compareBy((v) => v.order));
    },
    enabledGroupedViews() {
      const groups = new Map();
      this.enabledViews.forEach((view) => {
        const groupName = view.group;
        const group = groups.get(groupName) || {
          id: groupName,
          order: Number.MAX_SAFE_INTEGER,
          views: [],
        };
        groups.set(groupName, {
          ...group,
          order: Math.min(group.order, view.order),
          icon: VIEW_GROUP_ICON[groupName],
          views: [...group.views, view],
        });
      });
      return Array.from(groups.values());
    },
  },
  watch: {
    $route() {
      this.sidebarOpen = false;
    },
  },
  methods: {
    toggleSidebar() {
      this.sidebarOpen = !this.sidebarOpen;
    },
    getGroupTitle(groupId) {
      const key = 'sidebar.' + groupId + '.title';
      const translated = this.$t(key);
      return key === translated ? groupId : translated;
    },
    isActiveGroup(group) {
      return group.views.some((v) => toRaw(v) === this.$route.meta.view);
    },
    hasMultipleViews(group) {
      return group.views.length > 1;
    },
  },
});
</script>

<style scoped>
.instance-info-block {
  @apply bg-opacity-40 flex items-center text-sm py-4 px-6 text-left overflow-hidden text-ellipsis rounded transition duration-300 ease-in-out cursor-pointer;
}

.navbar-link {
  @apply cursor-pointer bg-opacity-40 duration-300 ease-in-out flex  items-center overflow-hidden py-4 rounded-md text-sm transition whitespace-nowrap text-gray-500;
}

.navbar-link__active {
  @apply text-black;
  .navbar-text {
    @apply text-black;
  }
}

.navbar-link:hover,
.navbar-link__group__active {
  @apply bg-opacity-80 bg-slate-100;
  .navbar-text {
    @apply text-black;
  }
}

.navbar-link__group_item {
  @apply h-6 mx-4 my-1 py-5 pl-12 pr-4;
}

.navbar-link__group {
  @apply h-6 mx-4 my-1 py-6 px-4;
}

.navbar-text {
  @apply text-gray-500;
}
</style>
