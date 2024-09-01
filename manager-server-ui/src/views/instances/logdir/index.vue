<template>
  <sm-instance-section :loading="!hasLoaded">
    <div class="flex h-[calc(100vh-110px)] border bg-white">
      <!-- 左侧文件目录 -->
      <div class="basis-1/5 p-4 border border-gray-100">
        <el-input
          v-model="filterText"
          class="w-full mb-1 border-0"
          placeholder="请输入关键字"
        />
        <el-tree
          v-cloak
          ref="treeRef"
          style="max-width: 600px"
          class="filter-tree"
          :data="logList"
          :props="defaultProps"
          default-expand-all
          highlight-current
          @node-click="selectFile"
          :filter-node-method="filterNode"
        >
          <template #default="{ node }">
            <span
              class="bg-opacity-80 cursor-pointer py-2 px-3 hover:bg-slate-100 m-1 transition-all duration-300 ease-in-out"
            >
              <span :title="node.data.name">
                <font-awesome-icon
                  v-if="node.data.type === 'file'"
                  class="mr-1 text-gray-300"
                  :icon="['fas', 'file']"
                />
                <font-awesome-icon
                  v-if="node.data.type === 'folder'"
                  class="mr-1 text-yellow-400"
                  :icon="['fas', 'folder']"
                />
                {{ node.data.name }}
              </span>
            </span>
          </template>
        </el-tree>
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
    logList: [],
    selectedFile: undefined,
    selectedFileContent: undefined,
    defaultProps: {
      children: 'children',
      label: 'name',
    },
    filterText: '',
  }),
  watch: {
    filterText(val) {
      this.$refs.treeRef.filter(val);
    },
  },
  computed: {
    processedLines() {
      return this.selectedFileContent
        ? this.selectedFileContent.map((line) => {
            // 用 <span> 标签包裹 INFO 字样，并应用样式
            return line
              .replace(/(INFO)/g, '<span class="info-text">\$1</span>')
              .replace(/(ERROR)/g, '<span class="error-text">\$1</span>');
          })
        : undefined;
    },
  },
  created() {
    this.getDirList();
  },
  methods: {
    getDirList() {
      return this.instance
        .logdir()
        .then((res) => {
          this.hasLoaded = true;
          this.logList = res.data;
        })
        .catch((error) => {
          this.hasLoaded = true;
          console.warn('Fetching logdir failed:', error);
          ElMessage.error('加载失败');
        });
    },
    selectFile(node) {
      if (node.type === 'folder') return;
      this.hasLoaded = false;
      this.selectedFile = node.name;
      const encodedParameter = encodeURIComponent(node.path);
      this.instance
        .logContent(encodedParameter)
        .then((res) => {
          this.selectedFileContent = res.data
            ? res.data.split('\n')
            : undefined;
          this.hasLoaded = true;
        })
        .catch((error) => {
          this.hasLoaded = true;
          console.warn('Fetching logdir failed:', error);
          ElMessage.error('加载失败');
        });
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.name.toLowerCase().includes(value.toLowerCase());
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

<style lang="scss">
.info-text {
  color: #4e952a;
}
.error-text {
  color: #e14c46;
}
</style>
