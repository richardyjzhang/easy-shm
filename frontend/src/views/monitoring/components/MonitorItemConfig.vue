<script setup lang="ts">
import { ListOutline, SaveOutline } from '@vicons/ionicons5'
import {
  NButton,
  NCard,
  NCheckbox,
  NDataTable,
  NEmpty,
  NIcon,
  NSelect,
  NSpin,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { computed, h, onMounted, ref, watch } from 'vue'

import { listDepartments } from '@/services/department'
import {
  getStructureMonitorConfigs,
  listMonitorIndexesWithValueTypes,
  saveStructureMonitorConfigs,
} from '@/services/structureMonitorConfig'
import { listStructures } from '@/services/structure'
import type { MonitorIndexWithValueTypes } from '@/types/structureMonitorConfig'

const message = useMessage()

const typeMap: Record<number, string> = {
  1: '环境',
  2: '作用',
  3: '结构响应',
  4: '结构变化',
}

// ===================== 顶部：结构物选择 =====================
const selectedStructureId = ref<number | null>(null)
const structureOptions = ref<SelectOption[]>([])
const departmentOptions = ref<SelectOption[]>([])
const structureLoading = ref(false)
const searchStructureDeptId = ref<number | null>(null)

async function loadDepartments() {
  try {
    const res = await listDepartments({ page: 1, size: 500 })
    departmentOptions.value = res.data.content.map((d) => ({
      label: d.name,
      value: d.id!,
    }))
    if (departmentOptions.value.length > 0 && searchStructureDeptId.value == null) {
      searchStructureDeptId.value = Number(departmentOptions.value[0].value)
    }
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载机构列表失败')
  }
}

async function loadStructures() {
  structureLoading.value = true
  try {
    const params: { name?: string; departmentId?: number; page: number; size: number } = {
      page: 1,
      size: 500,
    }
    if (searchStructureDeptId.value) {
      params.departmentId = searchStructureDeptId.value
    }
    const res = await listStructures(params)
    structureOptions.value = res.data.content.map((s) => ({
      label: s.name,
      value: s.id!,
    }))
    selectedStructureId.value =
      structureOptions.value.length > 0 ? Number(structureOptions.value[0].value) : null
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载结构物列表失败')
  } finally {
    structureLoading.value = false
  }
}

// ===================== 中间：监测项与监测内容配置 =====================
const monitorIndexes = ref<MonitorIndexWithValueTypes[]>([])
const configLoading = ref(false)
const dataLoading = ref(false)

// 已配置的监测内容ID集合
const configuredValueTypeIds = ref<Set<number>>(new Set())
// 当前选中的监测内容ID（临时状态）
const selectedValueTypeIds = ref<Set<number>>(new Set())

async function loadMonitorIndexes() {
  dataLoading.value = true
  try {
    const res = await listMonitorIndexesWithValueTypes()
    monitorIndexes.value = res.data
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载监测项失败')
  } finally {
    dataLoading.value = false
  }
}

async function loadStructureConfigs() {
  if (!selectedStructureId.value) {
    configuredValueTypeIds.value = new Set()
    selectedValueTypeIds.value = new Set()
    return
  }
  configLoading.value = true
  try {
    const res = await getStructureMonitorConfigs(selectedStructureId.value)
    const configs = res.data
    const ids = new Set(configs.map((c) => c.valueTypeId))
    configuredValueTypeIds.value = ids
    selectedValueTypeIds.value = new Set(ids) // 初始选中已配置的
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载配置失败')
  } finally {
    configLoading.value = false
  }
}

function isValueTypeChecked(valueTypeId: number): boolean {
  return selectedValueTypeIds.value.has(valueTypeId)
}

function toggleValueType(valueTypeId: number, checked: boolean) {
  const newSet = new Set(selectedValueTypeIds.value)
  if (checked) {
    newSet.add(valueTypeId)
  } else {
    newSet.delete(valueTypeId)
  }
  selectedValueTypeIds.value = newSet
}

const tableColumns: DataTableColumns<MonitorIndexWithValueTypes> = [
  {
    type: 'expand',
    expandable: (row) => row.valueTypes.length > 0,
    renderExpand: (row) => {
      if (row.valueTypes.length === 0) {
        return h('div', { class: 'py-2 pl-8 text-gray-400' }, '该监测项暂无监测内容')
      }
      return h(
        'div',
        { class: 'grid grid-cols-2 gap-2 py-2 pl-8 border-l border-(--n-border-color)' },
        row.valueTypes.map((valueType) =>
          h(
            NCheckbox,
            {
              checked: isValueTypeChecked(valueType.id!),
              disabled: !selectedStructureId.value,
              'onUpdate:checked': (checked: boolean) => toggleValueType(valueType.id!, checked),
            },
            {
              default: () =>
                `${valueType.name}${valueType.unit ? ` (${valueType.unit})` : ''}`,
            },
          ),
        ),
      )
    },
  },
  { title: '监测项', key: 'name', minWidth: 180, ellipsis: { tooltip: true } },
  { title: '类型', key: 'type', width: 120, render: (row) => typeMap[row.type] ?? row.type },
  { title: '监测内容数', key: 'valueTypeCount', width: 120, render: (row) => row.valueTypes.length },
]

// 计算变更：新增和删除的配置
const hasChanges = computed(() => {
  const original = configuredValueTypeIds.value
  const current = selectedValueTypeIds.value
  if (original.size !== current.size) return true
  for (const id of current) {
    if (!original.has(id)) return true
  }
  return false
})

const isSaving = ref(false)

async function handleSave() {
  if (!selectedStructureId.value) {
    message.warning('请先选择结构物')
    return
  }
  isSaving.value = true
  try {
    const valueTypeIds = Array.from(selectedValueTypeIds.value)
    await saveStructureMonitorConfigs(selectedStructureId.value, valueTypeIds)
    message.success('保存成功')
    // 更新原始配置
    configuredValueTypeIds.value = new Set(selectedValueTypeIds.value)
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    isSaving.value = false
  }
}

// ===================== 初始化 =====================
onMounted(async () => {
  await loadDepartments()
  await loadStructures()
  await loadMonitorIndexes()
})

// 监听结构物变化，加载配置
watch(selectedStructureId, () => {
  loadStructureConfigs()
})
</script>

<template>
  <n-card class="h-full" content-style="height: 100%; display: flex; flex-direction: column; padding: 16px;">
    <div class="flex items-center justify-between gap-3">
      <div class="flex items-center gap-2 text-base font-medium">
        <n-icon :component="ListOutline" size="18" />
        监测项配置
      </div>
      <n-button
        type="primary"
        :disabled="!selectedStructureId || !hasChanges"
        :loading="isSaving"
        @click="handleSave"
      >
        <template #icon>
          <n-icon :component="SaveOutline" />
        </template>
        保存配置
      </n-button>
    </div>

    <div class="mt-4 flex items-center gap-3 flex-nowrap overflow-x-auto pb-1">
      <span class="font-medium whitespace-nowrap">结构物</span>
      <n-select
        v-model:value="searchStructureDeptId"
        :options="departmentOptions"
        placeholder="所属机构"
        filterable
        style="width: 180px; flex: 0 0 auto;"
        @update:value="loadStructures"
      />
      <n-select
        v-model:value="selectedStructureId"
        :options="structureOptions"
        placeholder="请选择结构物"
        filterable
        :loading="structureLoading"
        style="width: 180px; flex: 0 0 auto;"
      />
    </div>

    <div class="mt-4 flex-1 min-h-0">
      <n-spin :show="dataLoading || configLoading">
        <n-empty v-if="monitorIndexes.length === 0 && !dataLoading" size="small" />
        <n-data-table
          v-else
          :row-key="(row: MonitorIndexWithValueTypes) => row.id ?? `${row.name}-${row.type}`"
          :columns="tableColumns"
          :data="monitorIndexes"
          :bordered="true"
          :single-line="false"
          size="small"
          max-height="100%"
        />
      </n-spin>
    </div>
  </n-card>
</template>
