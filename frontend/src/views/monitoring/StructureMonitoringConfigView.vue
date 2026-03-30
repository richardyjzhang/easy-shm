<script setup lang="ts">
import { ListOutline, LocationOutline, OptionsOutline } from '@vicons/ionicons5'
import { NIcon, NTabPane, NTabs } from 'naive-ui'
import { h, ref } from 'vue'

import MonitorItemConfig from './components/MonitorItemConfig.vue'
import PointManagement from './components/PointManagement.vue'
import ThresholdManagement from './components/ThresholdManagement.vue'

const activeTab = ref('items')

function renderTabLabel(label: string, icon: typeof LocationOutline) {
  return () => h('div', { class: 'flex items-center gap-1' }, [
    h(NIcon, { component: icon, size: 18 }),
    h('span', label),
  ])
}
</script>

<template>
  <div class="flex flex-col h-full">
    <n-tabs v-model:value="activeTab" type="line" class="flex-1" pane-class="h-full">
      <n-tab-pane name="items" :tab="renderTabLabel('监测项配置', ListOutline)">
        <MonitorItemConfig />
      </n-tab-pane>
      <n-tab-pane name="points" :tab="renderTabLabel('测点配置', LocationOutline)">
        <PointManagement />
      </n-tab-pane>
      <n-tab-pane name="thresholds" :tab="renderTabLabel('阈值配置', OptionsOutline)">
        <ThresholdManagement />
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<style scoped>
:deep(.n-tabs-nav) {
  padding: 0 16px;
  border-bottom: 1px solid var(--n-border-color);
}

:deep(.n-tab-pane) {
  padding: 16px;
}
</style>
