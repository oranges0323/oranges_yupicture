<template>
  <a-config-provider :locale="zhCN">
    <div id="app">
      <BasicLayout />
    </div>
  </a-config-provider>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import BasicLayout from "@/layouts/BasicLayout.vue";
import GlobalHeader from "@/components/GlobalHeader.vue";
import { healthUsingGet } from '@/api/mainController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

import zhCN from 'ant-design-vue/es/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
dayjs.locale('zh-cn');

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()

</script>

<style scoped>

</style>
