<template>
  <sm-instance-section :loading="!hasLoaded">
    <div class="flex h-[calc(100vh-110px)] border bg-white">
      <!-- 左侧文件目录 -->
      <div class="basis-1/5 p-4 border border-gray-100">
        <ul class="">
          <li
            v-for="(fileName, index) in logList"
            :key="fileName"
            :class="[
              active === index ? 'bg-slate-100 rounded' : '',
              'bg-opacity-80 cursor-pointer py-2 px-3 hover:bg-slate-100 m-1 transition-all duration-300 ease-in-out',
            ]"
            @click="selectFile(fileName, index)"
          >
            <font-awesome-icon
              class="mr-1 text-orange-400"
              :icon="['fas', 'file']"
            />
            {{ fileName }}
          </li>
        </ul>
      </div>
      <!-- 右侧文件内容展示 -->
      <div class="basis-4/5 overflow-auto p-4 text-sm">
        <h2 class="text-2xl font-bold mb-4">
          {{ selectedFile || '请选择一个文件' }}
        </h2>
        <div v-if="selectedFileContent" class="">
          <div
            v-for="(line, index) in processedLines"
            :key="index"
            class="py-0.5 whitespace-pre-wrap hover:bg-slate-200"
            v-html="line"
          ></div>
        </div>
        <div v-else>没有内容可显示</div>
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
    active: -1,
  }),
  computed: {
    processedLines() {
      return this.selectedFileContent
        ? this.selectedFileContent.map((line) => {
            // 用 <span> 标签包裹 INFO 字样，并应用样式
            return line.replace(
              /(INFO)/g,
              '<span class="info-text">\$1</span>',
            );
          })
        : undefined;
    },
  },
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
    selectFile(fileName, index) {
      this.active = index;
      this.selectedFile = fileName;
      this.hasLoaded = false;
      this.instance
        .logContent(fileName)
        .then((res) => {
          this.selectedFileContent = res.data
            ? res.data.split('\n')
            : undefined;
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
.info-text {
  color: #4e952a;
}
</style>
