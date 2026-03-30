import type { ApiResponse, MonitorPointThreshold } from '@/types/monitorPointThreshold'
import request from '@/utils/request'

export function listThresholdsByPointIds(pointIds: number[]) {
  return request.get<unknown, ApiResponse<MonitorPointThreshold[]>>('/monitor-point-thresholds', {
    params: { pointIds: pointIds.join(',') },
  })
}

export function getThresholdByPointId(pointId: number) {
  return request.get<unknown, ApiResponse<MonitorPointThreshold | null>>(
    `/monitor-point-thresholds/by-point/${pointId}`,
  )
}

export function saveThreshold(data: MonitorPointThreshold) {
  return request.post<unknown, ApiResponse<MonitorPointThreshold>>(
    '/monitor-point-thresholds',
    data,
  )
}

export function deleteThresholdByPointId(pointId: number) {
  return request.delete<unknown, ApiResponse<void>>(
    `/monitor-point-thresholds/by-point/${pointId}`,
  )
}
