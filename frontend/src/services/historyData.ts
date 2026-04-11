import type { ApiResponse, PointStatsData } from '@/types/historyData'
import request from '@/utils/request'

export function getHistoryData(params: {
  structureId: number
  valueTypeId: number
  period: string
  startTime: string
  endTime: string
}) {
  return request.get<unknown, ApiResponse<PointStatsData[]>>('/history-data', { params })
}
