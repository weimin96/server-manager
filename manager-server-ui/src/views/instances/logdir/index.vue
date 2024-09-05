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
          :filter-node-method="filterNode"
          @node-click="selectFile"
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
      <div class="basis-4/5 w-full p-4 text-sm">
        <sm-sticky-subnav>
          <div class="flex items-center justify-end gap-1">
            <div class="flex-1 text-xl font-bold">
              {{ selectedFile }}
            </div>
            <div class="mx-3 btn-group">
              <el-button :disabled="atTop" @click="scrollToTop">
                <svg
                  class="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M7 11l5-5m0 0l5 5m-5-5v12"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </el-button>
              <el-button :disabled="atBottom" @click="scrollToBottom">
                <svg
                  class="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M17 13l-5 5m0 0l-5-5m5 5V6"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </el-button>
            </div>

            <el-button class="hidden md:block" @click="downloadLogfile()">
              <font-awesome-icon icon="download" />&nbsp;
              <span>下载</span>
            </el-button>
          </div>
        </sm-sticky-subnav>
        <h2 v-if="!selectedFile" class="text-2xl font-bold mb-4">
          请选择一个文件
        </h2>
        <div v-if="selectedFileContent" class="mt-4">
          <DynamicScroller
            ref="scroller"
            :items="processedLines"
            key-field="index"
            :min-item-size="10"
            class="h-[calc(100vh-200px)]"
          >
            <template #default="{ item, index, active }">
              <DynamicScrollerItem
                :item="item"
                :active="active"
                :size-dependencies="[item.line]"
                :data-index="index"
              >
                <div
                  class="py-0.5 whitespace-normal hover:bg-slate-200"
                  v-html="item.line"
                />
              </DynamicScrollerItem>
            </template>
          </DynamicScroller>
        </div>
        <div v-else class="mt-4">没有内容可显示</div>
      </div>
    </div>
  </sm-instance-section>
</template>

<script>
import { DynamicScroller, DynamicScrollerItem } from 'vue-virtual-scroller';

import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';
import SmInstanceSection from '@/views/instances/shell/sm-instance-section.vue';

export default {
  components: { SmInstanceSection, DynamicScroller, DynamicScrollerItem },
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
    selectedPath: undefined,
    selectedFileContent: undefined,
    defaultProps: {
      children: 'children',
      label: 'name',
    },
    filterText: '',
    atTop: true,
    atBottom: false,
  }),
  computed: {
    processedLines() {
      return this.selectedFileContent
        ? this.selectedFileContent.map((line, index) => {
            // 用 <span> 标签包裹 INFO 字样，并应用样式
            return {
              index: index,
              line: line
                .replace(/(INFO)/g, '<span class="info-text">\$1</span>')
                .replace(/(ERROR)/g, '<span class="error-text">\$1</span>'),
            };
          })
        : undefined;
    },
  },
  watch: {
    filterText(val) {
      this.$refs.treeRef.filter(val);
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
      this.selectedPath = node.path;
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
    scrollToTop() {
      this.$refs.scroller.scrollToItem(0);
      this.atTop = true;
      this.atBottom = false;
    },
    scrollToBottom() {
      this.$refs.scroller.scrollToBottom();
      this.atTop = false;
      this.atBottom = true;
    },
    downloadLogfile() {
      const url = `/actuator/logcontent/${this.selectedPath}`;
      this.instance.downloadFile(url, this.selectedFile);
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
