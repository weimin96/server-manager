<template>
  <div class="hidden md:flex m-16">
    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3 w-full">
      <div class="rounded-xl border shadow bg-white">
        <div
          class="gap-y-1.5 p-6 flex flex-row items-center justify-between space-y-0 pb-2"
        >
          <h3 class="tracking-tight text-sm font-medium">应用数</h3>
          <svg
            class="h-6 w-6 text-muted-foreground"
            viewBox="0 0 1024 1024"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M464.081743 464.816989V169.827732A169.813593 169.813593 0 0 0 294.254011 0h-124.426279A169.813593 169.813593 0 0 0 0 169.827732v125.161525a169.827732 169.827732 0 0 0 169.827732 169.827732zM558.504322 464.816989h295.667946a169.841871 169.841871 0 0 0 169.827732-169.827732v-125.161525A169.827732 169.827732 0 0 0 854.172268 0h-125.840214A169.813593 169.813593 0 0 0 558.504322 169.827732zM464.081743 559.183011H169.827732A169.813593 169.813593 0 0 0 0 729.010743v125.161525a169.827732 169.827732 0 0 0 169.827732 169.827732h124.426279a169.827732 169.827732 0 0 0 169.827732-169.827732zM558.504322 559.183011v294.989257a169.827732 169.827732 0 0 0 169.771174 169.827732h125.840215a169.841871 169.841871 0 0 0 169.884289-169.827732v-125.161525a169.827732 169.827732 0 0 0-169.827732-169.827732z"
            ></path>
          </svg>
        </div>
        <div class="p-6 pt-1">
          <div class="text-4xl font-bold">{{ applicationsCount }}</div>
        </div>
      </div>
      <div class="rounded-xl border shadow bg-white">
        <div
          class="gap-y-1.5 p-6 flex flex-row items-center justify-between space-y-0 pb-2"
        >
          <h3 class="tracking-tight text-sm font-medium">实例数</h3>
          <svg
            class="h-6 w-6 text-muted-foreground"
            viewBox="0 0 1024 1024"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M941.568 239.104L536.576 5.12c-6.144-3.584-14.336-5.12-22.528-5.12-8.192 0-16.384 1.536-22.528 5.12L86.528 239.104c-12.288 7.168-22.528 24.576-22.528 38.912v467.456c0 14.336 10.24 31.744 22.528 38.912L491.52 1018.368c6.144 3.584 14.336 5.12 22.528 5.12 8.192 0 16.384-1.536 22.528-5.12l404.992-233.984c12.288-7.168 22.528-24.576 22.528-38.912V278.016c0-14.336-10.24-31.744-22.528-38.912z m-427.52-156.672l330.24 190.464-330.752 194.048L177.664 276.48l336.384-194.048z m38.4 837.12v-385.024l333.824-195.584V727.04l-333.824 192.512z"
            ></path>
          </svg>
        </div>
        <div class="p-6 pt-1">
          <div class="text-4xl font-bold">{{ instancesCount }}</div>
        </div>
      </div>
      <ApplicationStatusHero />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import ApplicationStatusHero from '@/views/applications/ApplicationStatusHero.vue';

const { applications } = useApplicationStore();
const applicationsCount = computed({
  get() {
    return applications.value.length;
  },
});

const instancesCount = computed({
  get() {
    return applications.value.reduce(
      (current, next) => current + next.instances.length,
      0,
    );
  },
});
</script>
