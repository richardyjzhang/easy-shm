<script setup lang="ts">
import { NConfigProvider, NMessageProvider } from 'naive-ui'
import { dateZhCN, zhCN } from 'naive-ui'

import { naiveThemeOverrides } from '@/config/theme'
</script>

<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN" :theme-overrides="naiveThemeOverrides">
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </n-config-provider>
</template>
