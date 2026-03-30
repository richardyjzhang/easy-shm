<script setup lang="ts">
import { CreateOutline, OptionsOutline } from '@vicons/ionicons5'
import {
  NButton,
  NCard,
  NDataTable,
  NEmpty,
  NForm,
  NFormItem,
  NIcon,
  NInputNumber,
  NModal,
  NSelect,
  NSpace,
  NSpin,
  NSwitch,
  NTag,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { computed, h, onMounted, ref, watch } from 'vue'

import { listDepartments } from '@/services/department'
import { listStructures } from '@/services/structure'
import { listMonitorPoints } from '@/services/monitorPoint'
import { listThresholdsByPointIds, saveThreshold } from '@/services/monitorPointThreshold'
import type { MonitorPoint } from '@/types/monitorPoint'
import type { MonitorPointThreshold } from '@/types/monitorPointThreshold'

const message = useMessage()

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

// ===================== 中间：测点列表 + 阈值 =====================
const points = ref<MonitorPoint[]>([])
const thresholdMap = ref<Map<number, MonitorPointThreshold>>(new Map())
const dataLoading = ref(false)

interface PointRow extends MonitorPoint {
  threshold?: MonitorPointThreshold
}

const tableData = computed<PointRow[]>(() =>
  points.value.map((p) => ({
    ...p,
    threshold: thresholdMap.value.get(p.id!),
  })),
)

async function loadData() {
  if (!selectedStructureId.value) {
    points.value = []
    thresholdMap.value = new Map()
    return
  }
  dataLoading.value = true
  try {
    const pointRes = await listMonitorPoints(selectedStructureId.value)
    points.value = pointRes.data

    if (pointRes.data.length > 0) {
      const pointIds = pointRes.data.map((p) => p.id!)
      const thresholdRes = await listThresholdsByPointIds(pointIds)
      thresholdMap.value = new Map(thresholdRes.data.map((t) => [t.pointId, t]))
    } else {
      thresholdMap.value = new Map()
    }
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载数据失败')
  } finally {
    dataLoading.value = false
  }
}

function renderThresholdRange(lower?: number | null, upper?: number | null) {
  if (lower == null && upper == null) return '未设置'
  const lo = lower != null ? String(lower) : '-∞'
  const hi = upper != null ? String(upper) : '+∞'
  return `${lo} ~ ${hi}`
}

const tableColumns = computed<DataTableColumns<PointRow>>(() => [
  { title: '测点编号', key: 'code', width: 260, ellipsis: { tooltip: true } },
  {
    title: '启用',
    key: 'enabled',
    width: 80,
    render: (row) => {
      const t = row.threshold
      if (!t) {
        return h(NTag, { size: 'small', type: 'default' }, { default: () => '未配置' })
      }
      return h(NTag, { size: 'small', type: t.enabled ? 'success' : 'warning' }, {
        default: () => (t.enabled ? '启用' : '停用'),
      })
    },
  },
  {
    title: '蓝色阈值',
    key: 'blue',
    width: 150,
    render: (row) => {
      const t = row.threshold
      if (!t) return '未设置'
      return renderThresholdRange(t.blueLower, t.blueUpper)
    },
  },
  {
    title: '黄色阈值',
    key: 'yellow',
    width: 150,
    render: (row) => {
      const t = row.threshold
      if (!t) return '未设置'
      return renderThresholdRange(t.yellowLower, t.yellowUpper)
    },
  },
  {
    title: '红色阈值',
    key: 'red',
    width: 150,
    render: (row) => {
      const t = row.threshold
      if (!t) return '未设置'
      return renderThresholdRange(t.redLower, t.redUpper)
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 4 }, () => [
        h(
          NButton,
          { text: true, type: 'primary', size: 'small', onClick: () => openEditDialog(row) },
          {
            default: () => '配置',
            icon: () => h(NIcon, { component: CreateOutline, size: 16 }),
          },
        ),
      ]),
  },
])

// ===================== 编辑弹窗 =====================
const dialogVisible = ref(false)
const formSaving = ref(false)
const editingPointId = ref<number | null>(null)
const editingPointCode = ref('')

const formModel = ref({
  enabled: true,
  blueLower: null as number | null,
  blueUpper: null as number | null,
  yellowLower: null as number | null,
  yellowUpper: null as number | null,
  redLower: null as number | null,
  redUpper: null as number | null,
})

function openEditDialog(row: PointRow) {
  editingPointId.value = row.id!
  editingPointCode.value = row.code
  const t = row.threshold
  if (t) {
    formModel.value = {
      enabled: t.enabled === 1,
      blueLower: t.blueLower ?? null,
      blueUpper: t.blueUpper ?? null,
      yellowLower: t.yellowLower ?? null,
      yellowUpper: t.yellowUpper ?? null,
      redLower: t.redLower ?? null,
      redUpper: t.redUpper ?? null,
    }
  } else {
    formModel.value = {
      enabled: true,
      blueLower: null,
      blueUpper: null,
      yellowLower: null,
      yellowUpper: null,
      redLower: null,
      redUpper: null,
    }
  }
  dialogVisible.value = true
}

function validateThresholds(): string | null {
  const { blueLower, blueUpper, yellowLower, yellowUpper, redLower, redUpper } = formModel.value

  if (blueLower != null && blueUpper != null && blueLower >= blueUpper)
    return '蓝色预警下限必须小于上限'
  if (yellowLower != null && yellowUpper != null && yellowLower >= yellowUpper)
    return '黄色预警下限必须小于上限'
  if (redLower != null && redUpper != null && redLower >= redUpper)
    return '红色预警下限必须小于上限'

  // 红色 ⊇ 黄色 ⊇ 蓝色（下限递减，上限递增）
  if (redLower != null && yellowLower != null && redLower > yellowLower)
    return '红色下限不能大于黄色下限'
  if (yellowLower != null && blueLower != null && yellowLower > blueLower)
    return '黄色下限不能大于蓝色下限'
  if (redUpper != null && yellowUpper != null && redUpper < yellowUpper)
    return '红色上限不能小于黄色上限'
  if (yellowUpper != null && blueUpper != null && yellowUpper < blueUpper)
    return '黄色上限不能小于蓝色上限'

  return null
}

async function handleSubmit(): Promise<boolean> {
  if (!editingPointId.value) return false
  const err = validateThresholds()
  if (err) {
    message.warning(err)
    return false
  }
  formSaving.value = true
  try {
    await saveThreshold({
      pointId: editingPointId.value,
      enabled: formModel.value.enabled ? 1 : 0,
      blueLower: formModel.value.blueLower,
      blueUpper: formModel.value.blueUpper,
      yellowLower: formModel.value.yellowLower,
      yellowUpper: formModel.value.yellowUpper,
      redLower: formModel.value.redLower,
      redUpper: formModel.value.redUpper,
    })
    message.success('保存成功')
    await loadData()
    return true
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '保存失败')
    return false
  } finally {
    formSaving.value = false
  }
}

// ===================== 初始化 =====================
onMounted(async () => {
  await loadDepartments()
  await loadStructures()
})

watch(selectedStructureId, () => {
  loadData()
})
</script>

<template>
  <n-card class="h-full" content-style="height: 100%; display: flex; flex-direction: column; padding: 16px;">
    <div class="flex items-center gap-2 text-base font-medium">
      <n-icon :component="OptionsOutline" size="18" />
      阈值配置
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
      <n-spin :show="dataLoading">
        <n-empty v-if="tableData.length === 0 && !dataLoading" size="small" description="暂无测点数据，请先在测点配置中添加测点" />
        <n-data-table
          v-else
          :row-key="(row: PointRow) => row.id!"
          :columns="tableColumns"
          :data="tableData"
          :bordered="true"
          :single-line="false"
          size="small"
          max-height="100%"
          :scroll-x="920"
        />
      </n-spin>
    </div>

    <!-- 阈值配置弹窗 -->
    <n-modal
      v-model:show="dialogVisible"
      preset="dialog"
      :title="`阈值配置 - ${editingPointCode}`"
      positive-text="保存"
      negative-text="取消"
      :loading="formSaving"
      style="width: 560px;"
      @positive-click="handleSubmit"
      @negative-click="dialogVisible = false"
    >
      <n-form label-placement="left" label-width="80" class="mt-4">
        <n-form-item label="启用阈值">
          <n-switch v-model:value="formModel.enabled" />
        </n-form-item>

        <div class="rounded-lg border border-blue-200 bg-blue-50 p-3 mb-3">
          <div class="font-medium text-blue-600 mb-2">蓝色预警</div>
          <div class="flex gap-3">
            <n-form-item label="下限" label-placement="left" label-width="40" class="flex-1 mb-0!">
              <n-input-number v-model:value="formModel.blueLower" placeholder="下限" class="w-full" clearable />
            </n-form-item>
            <n-form-item label="上限" label-placement="left" label-width="40" class="flex-1 mb-0!">
              <n-input-number v-model:value="formModel.blueUpper" placeholder="上限" class="w-full" clearable />
            </n-form-item>
          </div>
        </div>

        <div class="rounded-lg border border-yellow-200 bg-yellow-50 p-3 mb-3">
          <div class="font-medium text-yellow-600 mb-2">黄色预警</div>
          <div class="flex gap-3">
            <n-form-item label="下限" label-placement="left" label-width="40" class="flex-1 mb-0!">
              <n-input-number v-model:value="formModel.yellowLower" placeholder="下限" class="w-full" clearable />
            </n-form-item>
            <n-form-item label="上限" label-placement="left" label-width="40" class="flex-1 mb-0!">
              <n-input-number v-model:value="formModel.yellowUpper" placeholder="上限" class="w-full" clearable />
            </n-form-item>
          </div>
        </div>

        <div class="rounded-lg border border-red-200 bg-red-50 p-3">
          <div class="font-medium text-red-600 mb-2">红色预警</div>
          <div class="flex gap-3">
            <n-form-item label="下限" label-placement="left" label-width="40" class="flex-1 mb-0!">
              <n-input-number v-model:value="formModel.redLower" placeholder="下限" class="w-full" clearable />
            </n-form-item>
            <n-form-item label="上限" label-placement="left" label-width="40" class="flex-1 mb-0!">
              <n-input-number v-model:value="formModel.redUpper" placeholder="上限" class="w-full" clearable />
            </n-form-item>
          </div>
        </div>
      </n-form>
    </n-modal>
  </n-card>
</template>
