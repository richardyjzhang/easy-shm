import type { RouteRecordRaw } from 'vue-router'
import type { Component } from 'vue'

import { appDefaultRedirectPath, menuTree } from '@/config/menu'
import LoginLayout from '@/layouts/LoginLayout.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import LoginView from '@/views/login/LoginView.vue'
import PlaceholderView from '@/views/common/PlaceholderView.vue'
import StructureView from '@/views/basic/StructureView.vue'
import DepartmentView from '@/views/system/DepartmentView.vue'
import UserView from '@/views/system/UserView.vue'
import MonitorIndexView from '@/views/monitoring/MonitorIndexView.vue'
import DeviceModelView from '@/views/monitoring/DeviceModelView.vue'
import MonitorDeviceView from '@/views/monitoring/MonitorDeviceView.vue'
import StructureMonitoringConfigView from '@/views/monitoring/StructureMonitoringConfigView.vue'
import MonitorDataView from '@/views/monitoring/MonitorDataView.vue'

/** 已实现的业务页面：路由 name → 组件 */
const viewMap: Record<string, Component> = {
  'basic-structures': StructureView,
  'system-departments': DepartmentView,
  'system-users': UserView,
  'monitoring-data': MonitorDataView,
  'monitoring-items': MonitorIndexView,
  'monitoring-device-models': DeviceModelView,
  'monitoring-devices': MonitorDeviceView,
  'monitoring-structure-config': StructureMonitoringConfigView,
}

/** 由 `config/menu.ts` 的 menuTree 生成主布局子路由，避免与菜单配置重复维护 */
function buildMainLayoutChildren(): RouteRecordRaw[] {
  return menuTree.flatMap((g) =>
    g.items.map((it) => ({
      path: it.path,
      name: it.name,
      component: viewMap[it.name] ?? PlaceholderView,
      meta: { title: it.title },
    })),
  )
}

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: LoginLayout,
    children: [{ path: '', name: 'login', component: LoginView }],
  },
  {
    path: '/',
    component: MainLayout,
    redirect: appDefaultRedirectPath,
    children: buildMainLayoutChildren(),
  },
]
