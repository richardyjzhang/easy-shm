import type { RouteRecordRaw } from 'vue-router'

import { appDefaultRedirectPath, menuTree } from '@/config/menu'
import LoginLayout from '@/layouts/LoginLayout.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import LoginView from '@/views/login/LoginView.vue'
import PlaceholderView from '@/views/common/PlaceholderView.vue'

/** 由 `config/menu.ts` 的 menuTree 生成主布局子路由，避免与菜单配置重复维护 */
function buildMainLayoutChildren(): RouteRecordRaw[] {
  return menuTree.flatMap((g) =>
    g.items.map((it) => ({
      path: it.path,
      name: it.name,
      component: PlaceholderView,
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
