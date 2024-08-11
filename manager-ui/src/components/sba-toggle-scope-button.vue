

<template>
  <div>
    <sba-button
      v-if="instanceCount <= 1 || modelValue === APPLICATION"
      :class="classNames"
      :title="$t('term.affects_all_instances', { count: instanceCount })"
      class="w-full"
      size="sm"
      @click="toggleScope(ActionScope.INSTANCE)"
    >
      <span v-text="$t('term.application')" />
    </sba-button>
    <sba-button
      v-else
      :class="classNames"
      :title="$t('term.affects_this_instance_only')"
      class="w-full"
      size="sm"
      @click="toggleScope(ActionScope.APPLICATION)"
    >
      <span v-text="$t('term.instance')" />
    </sba-button>

    <p v-if="showInfo" class="text-center text-xs pt-1 truncate">
      <span
        v-if="modelValue === APPLICATION"
        v-text="$t('term.affects_all_instances', { count: instanceCount })"
      />
      <span v-else v-text="$t('term.affects_this_instance_only')" />
    </p>
  </div>
</template>

<script>
import { ActionScope } from '@/components/ActionScope';
import SbaButton from '@/components/sba-button';

export default {
  name: 'SbaToggleScopeButton',
  components: { SbaButton },
  props: {
    modelValue: {
      type: String,
      required: true,
    },
    instanceCount: {
      type: Number,
      required: true,
    },
    showInfo: {
      type: Boolean,
      default: true,
    },
  },
  emits: ['update:modelValue'],
  data() {
    return {
      ActionScope,
      APPLICATION: ActionScope.APPLICATION,
      INSTANCE: ActionScope.INSTANCE,
      classNames: [],
    };
  },
  methods: {
    toggleScope(newScope) {
      this.$emit('update:modelValue', newScope);
    },
  },
};
</script>
