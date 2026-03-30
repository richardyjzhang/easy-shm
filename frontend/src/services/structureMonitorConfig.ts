import type { ApiResponse, MonitorIndexWithValueTypes, StructureMonitorConfig } from '@/types/structureMonitorConfig'
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
export function listMonitorIndexesWithValueTypes(departmentId?: number) {
  return request.get<unknown, ApiResponse<MonitorIndexWithValueTypes[]>>('/monitor-indexes/with-value-types', {
    params: departmentId !== undefined ? { departmentId } : {},
  })
}
