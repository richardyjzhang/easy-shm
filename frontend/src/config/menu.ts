import type { MenuOption } from 'naive-ui'
import {
  AppsOutline,
  BarChartOutline,
  BusinessOutline,
  ColorPaletteOutline,
  CubeOutline,
  DocumentTextOutline,
  FolderOpenOutline,
  HardwareChipOutline,
  HomeOutline,
  ListOutline,
  OptionsOutline,
  PeopleOutline,
  PhoneLandscapeOutline,
  PulseOutline,
  SearchOutline,
  SettingsOutline,
  StorefrontOutline,
  WarningOutline,
} from '@vicons/ionicons5'
import { NIcon } from 'naive-ui'
import type { Component } from 'vue'
import { h } from 'vue'

/** 叶子菜单（相对主布局 path，无首 /；与路由 children、meta.title 同源） */
export type MenuLeafItem = {
  /** 路由 path 段，如 `dashboard/workspace` */
  path: string
  /** 菜单与页面标题 */
  title: string
  /** 路由 name */
  name: string
  icon: Component
}

export type MenuGroupConfig = {
  groupKey: string
  groupLabel: string
  groupIcon: Component
  items: MenuLeafItem[]
}

function itemIcon(Icon: Component) {
  return () => h(NIcon, { size: 18 }, { default: () => h(Icon) })
}

/**
 * 侧栏菜单与业务子路由的**唯一数据源**（分组、path、标题、图标、路由 name）
 */
export const menuTree: MenuGroupConfig[] = [
  {
    groupKey: 'sub-dashboard',
    groupLabel: '综合管理',
    groupIcon: AppsOutline,
    items: [
      { path: 'dashboard/workspace', title: '工作台', name: 'dashboard-workspace', icon: HomeOutline },
      { path: 'dashboard/query', title: '综合查询', name: 'dashboard-query', icon: SearchOutline },
      { path: 'dashboard/bim', title: 'BIM协同', name: 'dashboard-bim', icon: CubeOutline },
    ],
  },
  {
    groupKey: 'sub-basic',
    groupLabel: '基本信息',
    groupIcon: DocumentTextOutline,
    items: [
      { path: 'basic/structures', title: '结构管理', name: 'basic-structures', icon: StorefrontOutline },
      { path: 'basic/archives', title: '档案管理', name: 'basic-archives', icon: FolderOpenOutline },
    ],
  },
  {
    groupKey: 'sub-monitoring',
    groupLabel: '监测管理',
    groupIcon: PulseOutline,
    items: [
      { path: 'monitoring/data', title: '数据查看', name: 'monitoring-data', icon: BarChartOutline },
      {
        path: 'monitoring/alarm-handling',
        title: '报警处置',
        name: 'monitoring-alarm-handling',
        icon: WarningOutline,
      },
      { path: 'monitoring/items', title: '监测项管理', name: 'monitoring-items', icon: ListOutline },
      {
        path: 'monitoring/device-models',
        title: '设备信息管理',
        name: 'monitoring-device-models',
        icon: PhoneLandscapeOutline,
      },
      {
        path: 'monitoring/devices',
        title: '监测设备管理',
        name: 'monitoring-devices',
        icon: HardwareChipOutline,
      },
      {
        path: 'monitoring/structure-config',
        title: '结构监测配置',
        name: 'monitoring-structure-config',
        icon: OptionsOutline,
      },
    ],
  },
  {
    groupKey: 'sub-system',
    groupLabel: '系统配置',
    groupIcon: SettingsOutline,
    items: [
      {
        path: 'system/departments',
        title: '机构管理',
        name: 'system-departments',
        icon: BusinessOutline,
      },
      { path: 'system/users', title: '用户管理', name: 'system-users', icon: PeopleOutline },
      {
        path: 'system/personalization',
        title: '个性化',
        name: 'system-personalization',
        icon: ColorPaletteOutline,
      },
    ],
  },
]

/** 进入主布局后的默认重定向（第一个叶子） */
export const appDefaultRedirectPath = (() => {
  const first = menuTree[0]?.items[0]
  return first ? `/${first.path}` : '/'
})()

/** 叶子 path → 完整路径（与 router path 一致，供菜单选中） */
export function leafPathToFull(path: string) {
  return path.startsWith('/') ? path : `/${path}`
}

/** 带子菜单的一级项 + 叶子路由 */
export const menuOptions: MenuOption[] = menuTree.map((g) => ({
  label: g.groupLabel,
  key: g.groupKey,
  icon: itemIcon(g.groupIcon),
  children: g.items.map((it) => ({
    label: it.title,
    key: leafPathToFull(it.path),
    icon: itemIcon(it.icon),
  })),
}))

export const pathIconMap: Record<string, Component> = Object.fromEntries(
  menuTree.flatMap((g) => g.items.map((it) => [leafPathToFull(it.path), it.icon])),
)
