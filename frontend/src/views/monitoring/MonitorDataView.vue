<script setup lang="ts">
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'
import { NButton, NDatePicker, NEmpty, NSelect, NSpin, useMessage } from 'naive-ui'
import type { SelectOption } from 'naive-ui'
import { computed, onMounted, ref, watch } from 'vue'

import dayjs from 'dayjs'

import { listStructures } from '@/services/structure'
import {
  getStructureMonitorConfigs,
  listMonitorIndexesWithValueTypes,
} from '@/services/structureMonitorConfig'
import { getHistoryData } from '@/services/historyData'
import type { MonitorIndexWithValueTypes } from '@/types/structureMonitorConfig'
import type { PointStatsData } from '@/types/historyData'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent, DataZoomComponent])

const message = useMessage()

// ===================== 结构物选择 =====================
const selectedStructureId = ref<number | null>(null)
const structureOptions = ref<SelectOption[]>([])
const structureLoading = ref(false)

async function loadStructures() {
  structureLoading.value = true
  try {
    const res = await listStructures({ page: 1, size: 500 })
    structureOptions.value = res.data.content.map((s) => ({
      label: s.name,
      value: s.id!,
    }))
    if (!selectedStructureId.value && structureOptions.value.length > 0) {
      selectedStructureId.value = structureOptions.value[0].value as number
    }
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载结构物列表失败')
  } finally {
    structureLoading.value = false
  }
}

// ===================== 监测项 & 监测内容级联 =====================
const allIndexesWithVT = ref<MonitorIndexWithValueTypes[]>([])
const configuredValueTypeIds = ref<Set<number>>(new Set())

const selectedMonitorIndexId = ref<number | null>(null)
const selectedValueTypeId = ref<number | null>(null)

const monitorIndexOptions = computed<SelectOption[]>(() => {
  const configured = configuredValueTypeIds.value
  if (configured.size === 0) return []

  return allIndexesWithVT.value
    .filter((idx) => idx.valueTypes.some((vt) => configured.has(vt.id!)))
    .map((idx) => ({ label: idx.name, value: idx.id! }))
})

const valueTypeOptions = computed<SelectOption[]>(() => {
  if (!selectedMonitorIndexId.value) return []
  const configured = configuredValueTypeIds.value
  const idx = allIndexesWithVT.value.find((i) => i.id === selectedMonitorIndexId.value)
  if (!idx) return []
  return idx.valueTypes
    .filter((vt) => configured.has(vt.id!))
    .map((vt) => ({
      label: vt.name + (vt.unit ? ` (${vt.unit})` : ''),
      value: vt.id!,
    }))
})

async function loadAllMonitorData() {
  try {
    const res = await listMonitorIndexesWithValueTypes()
    allIndexesWithVT.value = res.data
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载监测项失败')
  }
}

async function loadStructureConfigs(structureId: number) {
  try {
    const res = await getStructureMonitorConfigs(structureId)
    configuredValueTypeIds.value = new Set(res.data.map((c) => c.valueTypeId))
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载结构配置失败')
  }
}

watch(selectedStructureId, async (val) => {
  selectedMonitorIndexId.value = null
  selectedValueTypeId.value = null
  configuredValueTypeIds.value = new Set()
  if (val) {
    await loadStructureConfigs(val)
    if (monitorIndexOptions.value.length > 0) {
      selectedMonitorIndexId.value = monitorIndexOptions.value[0].value as number
    }
  }
})

watch(selectedMonitorIndexId, () => {
  selectedValueTypeId.value = null
  if (selectedMonitorIndexId.value && valueTypeOptions.value.length > 0) {
    selectedValueTypeId.value = valueTypeOptions.value[0].value as number
  }
})

// ===================== 时间范围 & 粒度 =====================
const H = 3_600_000
const D = 86_400_000

const now = Date.now()
const timeRange = ref<[number, number]>([now - 7 * D, now])

const timeShortcuts = {
  '最近1小时': () => [Date.now() - 1 * H, Date.now()] as [number, number],
  '最近1天': () => [Date.now() - 1 * D, Date.now()] as [number, number],
  '最近3天': () => [Date.now() - 3 * D, Date.now()] as [number, number],
  '最近1周': () => [Date.now() - 7 * D, Date.now()] as [number, number],
  '最近1月': () => [Date.now() - 30 * D, Date.now()] as [number, number],
  '最近3月': () => [Date.now() - 90 * D, Date.now()] as [number, number],
}

const PERIOD_RULES = [
  { value: '1s', label: '1秒', maxMs: 1 * H, hint: '<= 1小时' },
  { value: '1min', label: '1分钟', maxMs: 3 * D, hint: '<= 3天' },
  { value: '10min', label: '10分钟', maxMs: 30 * D, hint: '<= 30天' },
  { value: '1h', label: '1小时', maxMs: 365 * D, hint: '<= 1年' },
]

const selectedPeriod = ref<string>('10min')

const timeSpanMs = computed(() => {
  if (!timeRange.value) return 0
  return timeRange.value[1] - timeRange.value[0]
})

const periodOptions = computed<SelectOption[]>(() => {
  const span = timeSpanMs.value
  return PERIOD_RULES.map((r) => {
    const disabled = span > 0 && span > r.maxMs
    return {
      label: disabled ? `${r.label}（需${r.hint}）` : r.label,
      value: r.value,
      disabled,
    }
  })
})

function bestPeriodForSpan(spanMs: number): string {
  for (const r of PERIOD_RULES) {
    if (spanMs <= r.maxMs) return r.value
  }
  return '1h'
}

watch(timeRange, (val) => {
  if (!val) return
  const span = val[1] - val[0]
  const rule = PERIOD_RULES.find((r) => r.value === selectedPeriod.value)
  if (rule && span > rule.maxMs) {
    selectedPeriod.value = bestPeriodForSpan(span)
  }
})

// ===================== 查询 & 图表 =====================
const queryLoading = ref(false)
const chartData = ref<PointStatsData[]>([])

const canQuery = computed(
  () =>
    selectedStructureId.value != null &&
    selectedValueTypeId.value != null &&
    selectedPeriod.value != null &&
    timeRange.value != null,
)

async function handleQuery() {
  if (!canQuery.value) {
    message.warning('请完整填写查询条件')
    return
  }
  queryLoading.value = true
  chartData.value = []
  try {
    const [start, end] = timeRange.value!
    const res = await getHistoryData({
      structureId: selectedStructureId.value!,
      valueTypeId: selectedValueTypeId.value!,
      period: selectedPeriod.value,
      startTime: dayjs(start).format('YYYY-MM-DD HH:mm:ss.SSS'),
      endTime: dayjs(end).format('YYYY-MM-DD HH:mm:ss.SSS'),
    })
    chartData.value = res.data
    if (res.data.length === 0 || res.data.every((p) => p.records.length === 0)) {
      message.info('查询范围内无数据')
    }
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '查询失败')
  } finally {
    queryLoading.value = false
  }
}

// 条件齐备时自动查询
watch(canQuery, (val) => {
  if (val) handleQuery()
})

const selectedUnit = computed(() => {
  if (!selectedValueTypeId.value) return ''
  for (const idx of allIndexesWithVT.value) {
    const vt = idx.valueTypes.find((v) => v.id === selectedValueTypeId.value)
    if (vt) return vt.unit || ''
  }
  return ''
})

function parseTs(ts: string): number {
  return new Date(ts.replace(' ', 'T')).getTime()
}

const PALETTE = [
  '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
  '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#48b8d0',
]

const chartOption = computed(() => {
  const data = chartData.value
  if (data.length === 0) return null

  const validPoints = data.filter((p) => p.records.length > 0)
  if (validPoints.length === 0) return null

  const series: Record<string, unknown>[] = []

  validPoints.forEach((p, idx) => {
    const label = p.pointCode + (p.pointLocation ? ` (${p.pointLocation})` : '')
    const color = PALETTE[idx % PALETTE.length]

    series.push({
      name: label,
      type: 'line',
      showSymbol: false,
      smooth: true,
      lineStyle: { width: 2, color },
      itemStyle: { color },
      emphasis: { lineStyle: { width: 3 } },
      z: 10,
      data: p.records.map((r) => [parseTs(r.ts), r.firstVal]),
    })

    series.push({
      name: label + '__min',
      type: 'line',
      stack: `envelope_${idx}`,
      showSymbol: false,
      smooth: true,
      lineStyle: { width: 0 },
      itemStyle: { color },
      emphasis: { disabled: true },
      z: 1,
      data: p.records.map((r) => [parseTs(r.ts), r.minVal]),
    })

    series.push({
      name: label + '__max',
      type: 'line',
      stack: `envelope_${idx}`,
      showSymbol: false,
      smooth: true,
      lineStyle: { width: 0 },
      itemStyle: { color },
      areaStyle: { color, opacity: 0.12 },
      emphasis: { disabled: true },
      z: 1,
      data: p.records.map((r) => [parseTs(r.ts), r.maxVal - r.minVal]),
    })
  })

  const spanMs = timeSpanMs.value
  let axisLabelFormat: string
  if (spanMs <= 1 * H) {
    axisLabelFormat = '{HH}:{mm}:{ss}'
  } else if (spanMs <= 3 * D) {
    axisLabelFormat = '{MM}-{dd} {HH}:{mm}'
  } else {
    axisLabelFormat = '{yyyy}-{MM}-{dd}'
  }

  const legendNames = validPoints.map(
    (p) => p.pointCode + (p.pointLocation ? ` (${p.pointLocation})` : ''),
  )

  return {
    tooltip: {
      trigger: 'axis' as const,
      axisPointer: { type: 'cross' as const, label: { backgroundColor: '#6a7985' } },
      formatter: (params: { seriesName: string; data: [number, number]; marker: string }[]) => {
        if (!params.length) return ''
        const time = dayjs(params[0].data[0]).format('YYYY-MM-DD HH:mm:ss')
        let html = `<div style="font-weight:600;margin-bottom:4px">${time}</div>`
        const mainParams = params.filter(
          (p) => !p.seriesName.endsWith('__min') && !p.seriesName.endsWith('__max'),
        )
        for (const p of mainParams) {
          const point = chartData.value.find(
            (pt) =>
              (pt.pointCode + (pt.pointLocation ? ` (${pt.pointLocation})` : '')) ===
              p.seriesName,
          )
          const rec = point?.records.find((r) => parseTs(r.ts) === p.data[0])
          html += `${p.marker} <b>${p.seriesName}</b><br/>`
          if (rec) {
            html += `&nbsp;&nbsp;首值: ${rec.firstVal?.toFixed(4) ?? '-'}`
            html += `&nbsp;&nbsp;均值: ${rec.avgVal?.toFixed(4) ?? '-'}<br/>`
            html += `&nbsp;&nbsp;最大: ${rec.maxVal?.toFixed(4) ?? '-'}`
            html += `&nbsp;&nbsp;最小: ${rec.minVal?.toFixed(4) ?? '-'}<br/>`
            html += `&nbsp;&nbsp;标准差: ${rec.stdVal?.toFixed(4) ?? '-'}`
            html += `&nbsp;&nbsp;个数: ${rec.countVal ?? '-'}<br/>`
          }
        }
        return html
      },
    },
    legend: {
      type: 'scroll' as const,
      top: 0,
      itemGap: 16,
      data: legendNames,
    },
    grid: {
      left: 70,
      right: 30,
      top: 40,
      bottom: 80,
      containLabel: false,
    },
    xAxis: {
      type: 'time' as const,
      axisLabel: {
        formatter: axisLabelFormat,
      },
      splitLine: { show: false },
    },
    yAxis: {
      type: 'value' as const,
      name: selectedUnit.value,
      nameLocation: 'middle' as const,
      nameGap: 55,
      splitLine: { lineStyle: { type: 'dashed' as const, color: '#e8e8e8' } },
    },
    dataZoom: [
      { type: 'inside' as const, xAxisIndex: 0 },
      {
        type: 'slider' as const,
        xAxisIndex: 0,
        bottom: 10,
        height: 28,
        labelFormatter: (_: number, val: string) => dayjs(val).format('MM-DD HH:mm'),
      },
    ],
    series,
  }
})

// ===================== 初始化 =====================
onMounted(async () => {
  await Promise.all([loadStructures(), loadAllMonitorData()])
  // loadStructures 已自动选中第一个结构物并触发 watcher，
  // 但 watcher 中 loadStructureConfigs 的 await 可能在 allIndexesWithVT 加载完成前就执行了，
  // 导致 monitorIndexOptions 为空。此处补偿：确保所有数据就绪后级联选中。
  if (selectedStructureId.value && configuredValueTypeIds.value.size === 0) {
    await loadStructureConfigs(selectedStructureId.value)
  }
  if (monitorIndexOptions.value.length > 0 && !selectedMonitorIndexId.value) {
    selectedMonitorIndexId.value = monitorIndexOptions.value[0].value as number
  }
})
</script>

<template>
  <div class="monitor-data-view">
    <!-- 筛选区域 -->
    <div class="filter-bar">
      <n-select
        v-model:value="selectedStructureId"
        :options="structureOptions"
        placeholder="选择结构物"
        filterable
        :loading="structureLoading"
        style="width: 180px"
      />
      <n-select
        v-model:value="selectedMonitorIndexId"
        :options="monitorIndexOptions"
        placeholder="选择监测项"
        filterable
        :disabled="!selectedStructureId"
        style="width: 180px"
      />
      <n-select
        v-model:value="selectedValueTypeId"
        :options="valueTypeOptions"
        placeholder="选择监测内容"
        filterable
        :disabled="!selectedMonitorIndexId"
        style="width: 200px"
      />
      <n-date-picker
        v-model:value="timeRange"
        type="datetimerange"
        :shortcuts="timeShortcuts"
        format="yyyy-MM-dd HH:mm:ss"
        clearable
        style="width: 430px"
      />
      <n-select
        v-model:value="selectedPeriod"
        :options="periodOptions"
        placeholder="统计粒度"
        style="width: 160px"
      />
      <n-button type="primary" :disabled="!canQuery" :loading="queryLoading" @click="handleQuery">
        查询
      </n-button>
    </div>

    <!-- 图表区域 -->
    <div class="chart-area">
      <n-spin :show="queryLoading" class="chart-spin">
        <v-chart
          v-if="chartOption"
          :option="chartOption"
          autoresize
          class="chart-instance"
        />
        <n-empty v-else description="请选择查询条件后点击查询" style="padding-top: 120px" />
      </n-spin>
    </div>
  </div>
</template>

<style scoped>
.monitor-data-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 16px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.chart-area {
  flex: 1;
  min-height: 480px;
  overflow: hidden;
}

.chart-spin {
  width: 100%;
  height: 100%;
}

.chart-instance {
  width: 100%;
  height: 100%;
  min-height: 480px;
}
</style>
