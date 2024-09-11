<template>
  <sm-instance-section :loading="!hasLoaded">
    <div class="flex h-[calc(100vh-110px)] border bg-white">
      <div class="flex flex-col w-full p-4">
        <div class="flex justify-end">
          <span class="mr-2 leading-8">刷新时间</span>
          <div class="w-20 mr-2">
            <el-select
              v-model="timeValue"
              placeholder="Select"
              @change="changeTimer"
            >
              <el-option
                v-for="item in timeOptions"
                :key="item"
                :label="item + 's'"
                :value="item"
              />
            </el-select>
          </div>
          <el-button type="primary" @click="stopTimer">暂停刷新</el-button>
        </div>
        <el-table
          ref="table"
          class="mb-6 w-full flex"
          :data="sqlList"
          tooltip-effect="dark myTooltips"
          stripe
        >
          <el-table-column label="SQL" show-overflow-tooltip>
            <template #default="scope">
              <span
                class="text-blue-500 hover:text-blue-700 cursor-default"
                @click="previewSql(scope.row)"
              >
                {{ scope.row.SQL }}</span
              >
            </template>
          </el-table-column>
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
        </el-table>
        <div class="flex justify-end">
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
    <el-dialog
      v-model="dialogSqlPreviewVisible"
      title="查看"
      width="500"
      :modal="false"
    >
      <div
        class="bg-black text-white transition-colors duration-500 my-4 rounded-lg overflow-x-auto"
      >
        <div class="">
          <div class="relative">
            <span class="absolute right-2 top-0.5 text-gray-500">{{
              dbType
            }}</span>
          </div>
          <div class="px-4 py-4" v-html="formattedSql"></div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="copyText()"> 复制 </el-button>
          <el-button @click="dialogSqlPreviewVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </sm-instance-section>
</template>

<script>
import moment from 'moment/moment';
import { switchMap } from 'rxjs/operators';
import { format } from 'sql-formatter';

import Instance from '@/services/instance';
import { timer } from '@/utils/rxjs';
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
    timeValue: 10,
    timeOptions: [5, 10, 30, 60, 120],
    subscription: null,
    content: '',
    dbType: '',
    dialogSqlPreviewVisible: false,
    supportsDbType: ['plsql', 'mysql', 'postgresql', 'sqlite', 'mariadb'],
  }),
  computed: {
    formattedSql() {
      return this.content
        .replace(/\n/g, '<br>')
        .replace(/ {1,}/g, (match) =>
          '<span class="mr-2"></span>'.repeat(match.length),
        );
    },
  },
  created() {
    this.createSubscription();
  },
  unmounted() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  },
  methods: {
    createSubscription() {
      this.subscription = timer(0, this.timeValue * 1000)
        .pipe(switchMap(() => this.getSqlList()))
        .subscribe((res) => {
          this.hasLoaded = true;
          this.sqlList = res.data.records;
          this.total = res.data.total;
        });
    },
    getSqlList() {
      this.hasLoaded = false;
      return this.instance.druid(this.current, this.pageSize);
    },
    previewSql(row) {
      this.dbType = row.DbType;
      let formatType;
      if (this.supportsDbType.indexOf(row.DbType) === -1) {
        formatType = 'sql';
      } else {
        formatType = row.DbType;
      }
      this.content = format(row.SQL, { language: formatType });
      this.dialogSqlPreviewVisible = true;
    },
    formatter(row) {
      return moment(row.LastTime, moment.HTML5_FMT.DATETIME_LOCAL).format(
        'YYYY-MM-DD HH:mm',
      );
    },
    copyText() {
      navigator.clipboard
        .writeText(this.content)
        .then(() => {
          ElMessage.success('文本已复制');
        })
        .catch((err) => {
          console.error('复制失败', err);
        });
    },
    changeTimer() {
      if (this.subscription) {
        this.subscription.unsubscribe();
      }
      this.createSubscription();
    },
    stopTimer() {
      if (this.subscription) {
        this.subscription.unsubscribe();
        ElMessage.success('暂停成功');
      }
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

<style lang="scss">
.myTooltips {
  width: 25%;
}
</style>
