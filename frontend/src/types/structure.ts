import type { ApiResponse, PageResult } from '@/types/department'

export interface Structure {
  id?: number
  name: string
  type: string
  departmentId: number
  longitude?: number | null
  latitude?: number | null
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export type { ApiResponse, PageResult }
