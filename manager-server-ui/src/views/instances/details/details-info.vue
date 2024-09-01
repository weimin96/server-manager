<template>
  <sm-panel :title="$t('instances.details.info.title')" :loading="loading">
    <div class="content info">
      <table v-if="!isEmptyInfo" class="table">
        <tr v-for="(value, key) in info" :key="key">
          <td class="info__key" v-text="key" />
          <td>
            <sm-formatted-obj :value="value" />
          </td>
        </tr>
      </table>
      <p
        v-else
        class="is-muted"
        v-text="$t('instances.details.info.no_info_provided')"
      />
    </div>
  </sm-panel>
</template>

<script>
import Instance from '@/services/instance';

export default {
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    loading: false,
    liveInfo: null,
  }),
  computed: {
    info() {
      return this.liveInfo || this.instance.info;
    },
    isEmptyInfo() {
      return Object.keys(this.info).length <= 0;
    },
  },
  created() {
    this.fetchInfo();
  },
  methods: {
    async fetchInfo() {
      if (this.instance.hasEndpoint('info')) {
        this.loading = true;
        try {
          const res = await this.instance.fetchInfo();
          this.liveInfo = res.data;
        } catch (error) {
          ElMessage.error(error);
          console.warn('Fetching info failed:', error);
        } finally {
          this.loading = false;
        }
      }
    },
  },
};
</script>

<style lang="css">
.info {
  overflow: auto;
}

.info__key {
  vertical-align: top;
}
</style>
