import type { ApiResponse, PageResult } from '@/types/department'

export interface MonitorDevice {
  id?: number
  departmentId: number
  deviceModelId: number
  sn: string
  productionDate?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export type { ApiResponse, PageResult }
