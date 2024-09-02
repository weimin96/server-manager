<template>
  <sm-instance-section :loading="!hasLoaded">
    <div class="flex h-[calc(100vh-110px)] border bg-white">
      <div class="w-full p-4">
        <div class="float-right flex h-16">
          <span class="mr-2">刷新时间</span>
          <div class="w-20 mr-2">
            <el-select v-model="timeValue" placeholder="Select">
              <el-option
                v-for="item in timeOptions"
                :key="item"
                :label="item + 's'"
                :value="item"
              />
            </el-select>
          </div>

          <el-button type="primary">暂停刷新</el-button>
        </div>
        <el-table class="mb-6 w-full" :data="sqlList" stripe >
          <el-table-column prop="SQL" label="SQL" show-overflow-tooltip />
          <el-table-column prop="ExecuteCount" label="执行数" width="80" />
          <el-table-column prop="TotalTime" label="执行时间" width="80" />
          <el-table-column prop="MaxTimespan" label="最慢" width="60" />
          <el-table-column
            prop="InTransactionCount"
            label="事务执行"
            width="80"
          />
          <el-table-column prop="ErrorCount" label="错误数" width="80" />
          <el-table-column
            prop="EffectedRowCountMax"
            label="更新行数"
            width="80"
          />
          <el-table-column prop="ReaderOpenCount" label="读取行数" width="80" />
          <el-table-column prop="RunningCount" label="执行中" width="70" />
          <el-table-column prop="ConcurrentMax" label="最大并发" width="80" />
          <el-table-column
            prop="LastTime"
            label="最后执行时间"
            width="180"
            :formatter="formatter"
          />
          <el-table-column label="操作" min-width="60">
            <template #default>
              <el-button link type="primary" size="small" @click="handleClick">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="float-right">
          <el-pagination
            v-model:current-page="current"
            v-model:page-size="pageSize"
            background
            layout="prev, pager, next, sizes"
            :total="total"
            :page-sizes="[10, 25, 50, 100]"
          />
        </div>
      </div>
    </div>
  </sm-instance-section>
</template>

<script>
import moment from 'moment/moment';

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
    sqlList: [],
    current: 1,
    pageSize: 25,
    total: 0,
    timeValue: 5,
    timeOptions: [5, 10, 30, 60],
  }),
  created() {
    this.getSqlList();
  },
  methods: {
    getSqlList() {
      return this.instance
        .druid(this.current, this.pageSize)
        .then((res) => {
          this.hasLoaded = true;
          this.sqlList = res.data.records;
          this.total = res.data.total;
        })
        .catch((error) => {
          this.hasLoaded = true;
          console.warn('加载失败:', error);
          ElMessage.error('加载失败');
        });
    },
    handleClick(item) {
      console.log(item);
    },
    formatter(row) {
      return moment(row.LastTime, moment.HTML5_FMT.DATETIME_LOCAL).format(
        'YYYY-MM-DD HH:mm:ss',
      );
    },
  },
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/druid',
      parent: 'instances',
      path: 'druid',
      component: this,
      label: 'instances.druid.label',
      group: VIEW_GROUP.SQL,
      order: 200,
      isEnabled: ({ instance }) => instance.hasEndpoint('druid'),
    });
  },
};
</script>

<style lang="scss"></style>
