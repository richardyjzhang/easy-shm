import type {
  ApiResponse,
  MonitorIndexWithValueTypes,
  MonitorValueTypeWithIndex,
  StructureMonitorConfig,
} from '@/types/structureMonitorConfig'
import request from '@/utils/request'

// 获取结构物已配置的监测内容列表
export function getStructureMonitorConfigs(structureId: number) {
  return request.get<unknown, ApiResponse<StructureMonitorConfig[]>>('/structure-monitor-configs', {
    params: { structureId },
  })
}

// 批量保存结构物监测配置（全量替换）
export function saveStructureMonitorConfigs(structureId: number, valueTypeIds: number[]) {
  return request.post<unknown, ApiResponse<void>>('/structure-monitor-configs', valueTypeIds, {
    params: { structureId },
  })
}

// 删除单个配置
export function deleteStructureMonitorConfig(id: number) {
  return request.delete<unknown, ApiResponse<void>>(`/structure-monitor-configs/${id}`)
}

// 获取所有监测项及其监测内容（用于配置界面展示）
export async function listMonitorIndexesWithValueTypes(departmentId?: number) {
  const res = await request.get<unknown, ApiResponse<MonitorValueTypeWithIndex[]>>('/monitor-value-types')
  const map = new Map<number, MonitorIndexWithValueTypes>()
  for (const item of res.data) {
    const monitorIndexId = item.monitorIndexId
    if (departmentId !== undefined && item.monitorIndexDepartmentId !== departmentId) {
      continue
    }
    if (!map.has(monitorIndexId)) {
      map.set(monitorIndexId, {
        id: monitorIndexId,
        departmentId: item.monitorIndexDepartmentId ?? 0,
        type: item.monitorIndexType ?? 1,
        name: item.monitorIndexName ?? `监测项-${monitorIndexId}`,
        code: item.monitorIndexCode,
        valueTypes: [],
      })
    }
    map.get(monitorIndexId)!.valueTypes.push({
      id: item.id,
      monitorIndexId: item.monitorIndexId,
      name: item.name,
      unit: item.unit,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    })
  }
  return {
    ...res,
    data: Array.from(map.values()),
  } as ApiResponse<MonitorIndexWithValueTypes[]>
}
