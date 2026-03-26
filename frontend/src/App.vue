<script setup lang="ts">
import { NConfigProvider } from 'naive-ui'
import { dateZhCN, zhCN } from 'naive-ui'

import { naiveThemeOverrides } from '@/config/theme'
</script>

<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN" :theme-overrides="naiveThemeOverrides">
    <router-view />
  </n-config-provider>
</template>
