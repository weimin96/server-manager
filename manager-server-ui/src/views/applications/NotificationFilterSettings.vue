<template>
  <sm-panel class="shadow-xl">
    <template v-if="!activeFilter">
      <div class="field">
        <p class="control has-inline-text">
          <span
            v-html="
              t('applications.suppress_notifications_on', {
                name: object.id || object.name,
              })
            "
          />&nbsp;
          <sm-select
            v-model="ttl"
            class="inline-flex"
            name="ttl"
            :options="ttlOptions"
            @click.stop
          />
        </p>
      </div>
      <div class="field is-grouped is-grouped-right">
        <div class="control">
          <sm-button
            :class="{ 'is-loading': actionState === 'executing' }"
            @click.stop="addFilter"
          >
            <font-awesome-icon icon="bell-slash" />&nbsp;<span
              v-text="t('term.suppress')"
            />
          </sm-button>
        </div>
      </div>
    </template>
    <template v-else>
      <div class="field">
        <p class="control has-inline-text">
          <span
            v-html="
              t('applications.notifications_suppressed_for', {
                name: object.id || object.name,
              })
            "
          />&nbsp;
          <strong
            v-text="
              activeFilter.expiry
                ? activeFilter.expiry.locale(currentLocale).fromNow(true)
                : t('term.ever')
            "
          />.
        </p>
      </div>
      <div class="field is-grouped is-grouped-right">
        <div class="control">
          <sm-button
            :class="{ 'is-loading': actionState === 'executing' }"
            @click.stop="deleteActiveFilter"
          >
            <font-awesome-icon icon="bell" />&nbsp;<span
              v-text="t('term.unsuppress')"
            />
          </sm-button>
        </div>
      </div>
    </template>
  </sm-panel>
</template>
<script>
import { useI18n } from 'vue-i18n';

export default {
  props: {
    object: {
      type: Object,
      required: true,
    },
    notificationFilters: {
      type: Array,
      required: true,
    },
  },
  emits: ['filter-deleted', 'filter-added', 'filter-remove', 'filter-add'],
  setup() {
    const i18n = useI18n();
    return {
      t: i18n.t,
      currentLocale: i18n.locale,
    };
  },
  data() {
    return {
      ttl: 5 * 60 * 1000,
      ttlOptions: [
        { label: this.t('term.minutes', { count: 5 }), value: 5 * 60 * 1000 },
        { label: this.t('term.minutes', { count: 15 }), value: 15 * 60 * 1000 },
        { label: this.t('term.minutes', { count: 30 }), value: 30 * 60 * 1000 },
        { label: this.t('term.hours', { count: 1 }), value: 60 * 60 * 1000 },
        {
          label: this.t('term.hours', { count: 3 }),
          value: 3 * 60 * 60 * 1000,
        },
        {
          label: this.t('term.hours', { count: 8 }),
          value: 8 * 60 * 60 * 1000,
        },
        {
          label: this.t('term.hours', { count: 24 }),
          value: 24 * 60 * 60 * 1000,
        },
        { label: this.t('term.ever'), value: -1 },
      ],
      actionState: null,
    };
  },
  computed: {
    activeFilter() {
      return this.notificationFilters.find((f) => f.affects(this.object));
    },
  },
  methods: {
    async addFilter() {
      this.$emit('filter-add', {
        object: this.object,
        ttl: this.ttl,
      });
    },
    async deleteActiveFilter() {
      this.$emit('filter-remove', this.activeFilter);
    },
  },
};
</script>

<style>
.control.has-inline-text {
  line-height: 2.25em;
}
</style>
