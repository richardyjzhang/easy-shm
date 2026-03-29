import type { ApiResponse, PageResult } from '@/types/department'

export interface DeviceModel {
  id?: number
  departmentId: number
  manufacturer: string
  deviceType: string
  model: string
  range?: string
  principleType?: string
  sensitivity?: string
  accuracy?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export type { ApiResponse, PageResult }
