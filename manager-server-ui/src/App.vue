<template>
  <el-config-provider :locale="zhCn">
    <sm-shell/>
  </el-config-provider>
</template>

<script setup lang="ts">
import { ElConfigProvider } from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import SmShell from '@/shell/index.vue';


</script>
