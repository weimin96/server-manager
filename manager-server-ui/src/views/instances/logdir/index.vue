<template>
  <sm-instance-section :error="error" :loading="!hasLoaded">
    <div class="flex h-screen">
      <!-- 左侧文件目录 -->
      <div class="w-1/4 bg-gray-100 p-4 overflow-y-auto">
        <ul>
          <li
            v-for="fileName in logList"
            :key="fileName"
            class="cursor-pointer p-2 hover:bg-gray-200"
            @click="selectFile(fileName)"
          >
            {{ fileName }}
          </li>
        </ul>
      </div>
      <!-- 右侧文件内容展示 -->
      <div class="w-3/4 p-4">
        <h2 class="text-2xl font-bold mb-4">
          {{ selectedFile || '请选择一个文件' }}
        </h2>
        <div class="whitespace-pre-wrap">
          {{ selectedFileContent || '没有内容可显示' }}
        </div>
      </div>
    </div>
  </sm-instance-section>
</template>

<script>
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

export default {
  components: { SmInstanceSection },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  data: () => ({
    hasLoaded: false,
    error: null,
    logList: [],
    selectedFile: undefined,
    selectedFileContent: undefined,
  }),
  created() {
    this.getDirList();
  },
  methods: {
    getDirList() {
      this.error = null;
      return this.instance
        .logdir()
        .then((res) => {
          this.hasLoaded = true;
          this.logList = res.data;
        })
        .catch((error) => {
          this.hasLoaded = true;
          console.warn('Fetching logdir failed:', error);
          this.error = error;
        });
    },
    selectFile(fileName) {
      this.selectedFile = fileName;
      this.hasLoaded = false;
      this.instance
        .logContent(fileName)
        .then((res) => {
          this.selectedFileContent = res.data;
          this.hasLoaded = true;
        })
        .catch((error) => {
          this.hasLoaded = true;
          console.warn('Fetching logdir failed:', error);
          this.error = error;
        });
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/logdir',
      parent: 'instances',
      path: 'logdir',
      component: this,
      label: 'instances.logdir.label',
      group: VIEW_GROUP.LOGGING,
      order: 200,
      isEnabled: ({ instance }) => instance.hasEndpoint('logdir'),
    });
  },
};
</script>

<style lang="css">
.log-viewer pre {
  padding: 0 0.75em;
  margin-bottom: 1px;
}

.log-viewer pre:hover {
  background: #dbdbdb;
}

.log-viewer.wrap-lines pre {
  @apply whitespace-pre-wrap;
}

.log-viewer {
  background-color: #fff;
  overflow: auto;
}
</style>
