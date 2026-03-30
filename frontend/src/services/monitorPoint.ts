import type { ApiResponse, MonitorPoint } from '@/types/monitorPoint'
import request from '@/utils/request'

export function listMonitorPoints(structureId: number) {
  return request.get<unknown, ApiResponse<MonitorPoint[]>>('/monitor-points', {
    params: { structureId },
  })
}

export function getMonitorPoint(id: number) {
  return request.get<unknown, ApiResponse<MonitorPoint>>(`/monitor-points/${id}`)
}

export function createMonitorPoint(data: MonitorPoint) {
  return request.post<unknown, ApiResponse<MonitorPoint>>('/monitor-points', data)
}

export function updateMonitorPoint(id: number, data: MonitorPoint) {
  return request.put<unknown, ApiResponse<MonitorPoint>>(`/monitor-points/${id}`, data)
}

export function deleteMonitorPoint(id: number) {
  return request.delete<unknown, ApiResponse<void>>(`/monitor-points/${id}`)
}
