<template>
  <sm-instance-section :loading="!hasLoaded">
    <div class="flex h-[calc(100vh-110px)] border bg-white">
      <!-- 左侧文件目录 -->
      <div class="flex flex-col basis-1/5 p-4 border border-gray-100">
        <el-input
          v-model="filterText"
          class="w-full mb-1 border-0"
          placeholder="请输入关键字"
        />
        <el-tree
          v-cloak
          ref="treeRef"
          style="max-width: 600px"
          class="filter-tree max-h-full overflow-y-auto mt-2"
          :data="logList"
          :props="defaultProps"
          default-expand-all
          highlight-current
          :filter-node-method="filterNode"
          @node-click="selectFile"
        >
          <template #default="{ node }">
            <div
              class="bg-opacity-80 cursor-pointer py-2 px-3 m-1 transition-all duration-300 ease-in-out"
            >
              <div class="flex">
                <svg
                  v-if="node.data.type === 'file'"
                  class="mr-1 h-5 w-5"
                  viewBox="0 0 1024 1024"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M0 0h1024v1024H0z" fill="#FFFFFF"></path>
                  <path
                    d="M859.008 980.608H164.992a34.56 34.56 0 0 1-34.56-34.56V77.952a34.56 34.56 0 0 1 34.56-34.56H614.4a34.304 34.304 0 0 1 23.808 9.472L883.2 285.696a34.432 34.432 0 0 1 10.752 25.6v634.752a34.56 34.56 0 0 1-34.944 34.56zM199.552 911.36h624.896V325.632L600.32 112.64H199.552z"
                    fill="#0082FD"
                  ></path>
                  <path
                    d="M277.376 380.288h469.12v69.248H277.376zM277.376 512h469.12v69.248H277.376zM277.376 648.32h469.12v69.248H277.376zM594.688 778.624h151.936v69.248H594.688zM272.384 220.032h234.624v69.248H272.384z"
                    fill="#ABE3FF"
                  ></path>
                  <path
                    d="M851.968 345.6H614.4a34.304 34.304 0 0 1-24.576-10.24 34.688 34.688 0 0 1-10.112-24.576l1.408-233.088a34.56 34.56 0 0 1 58.88-24.32l236.288 232.704a34.688 34.688 0 0 1-24.32 59.52zM648.96 276.096H768l-117.76-115.968z"
                    fill="#0082FD"
                  ></path>
                </svg>
                <svg
                  v-if="node.data.type === 'folder'"
                  class="mr-1 h-5 w-5"
                  viewBox="0 0 1024 1024"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M918.673 883H104.327C82.578 883 65 867.368 65 848.027V276.973C65 257.632 82.578 242 104.327 242h814.346C940.422 242 958 257.632 958 276.973v571.054C958 867.28 940.323 883 918.673 883z"
                    fill="#FFE9B4"
                  ></path>
                  <path
                    d="M512 411H65V210.37C65 188.597 82.598 171 104.371 171h305.92c17.4 0 32.71 11.334 37.681 28.036L512 411z"
                    fill="#FFB02C"
                  ></path>
                  <path
                    d="M918.673 883H104.327C82.578 883 65 865.42 65 843.668V335.332C65 313.58 82.578 296 104.327 296h814.346C940.422 296 958 313.58 958 335.332v508.336C958 865.32 940.323 883 918.673 883z"
                    fill="#FFCA28"
                  ></path>
                </svg>
                <span>{{ node.data.name }}</span>
              </div>
            </div>
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
        <h2 v-if="!selectedFile" class="text-2xl font-bold mb-4 mt-2">
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
      const url = `/monitor/logcontent/${this.selectedPath}`;
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
