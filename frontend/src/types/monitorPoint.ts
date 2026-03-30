import type { ApiResponse, PageResult } from '@/types/department'

export interface MonitorPoint {
  id?: number
  structureId: number
  valueTypeId: number
  code: string
  location?: string
  deviceId: number
  channel?: string
  createdAt?: string
  updatedAt?: string
}

export type { ApiResponse, PageResult }
