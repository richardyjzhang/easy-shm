import type { GlobalThemeOverrides } from 'naive-ui'

/** 主题色（单一来源；修改后请同步 `src/styles/main.css` 中 `@theme` 的 primary 系列） */
export const THEME_PRIMARY = '#134E8E'
export const THEME_PRIMARY_HOVER = '#1860a8'
export const THEME_PRIMARY_ACTIVE = '#0f3f6f'

/**
 * 全局圆角 lg（与 Tailwind `rounded-lg`、下方 CSS `--radius-lg` 一致）
 * Naive 多数组件从 `common.borderRadius` 派生，改一处即可整站生效
 */
export const THEME_RADIUS_LG = '0.5rem'

export const naiveThemeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: THEME_PRIMARY,
    primaryColorHover: THEME_PRIMARY_HOVER,
    primaryColorPressed: THEME_PRIMARY_ACTIVE,
    primaryColorSuppl: THEME_PRIMARY_HOVER,
    borderRadius: THEME_RADIUS_LG,
    borderRadiusSmall: THEME_RADIUS_LG,
    scrollbarBorderRadius: THEME_RADIUS_LG,
  },
}
