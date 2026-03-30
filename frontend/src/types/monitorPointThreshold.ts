import type { ApiResponse } from '@/types/department'

export interface MonitorPointThreshold {
  id?: number
  pointId: number
  enabled: number
  blueLower?: number | null
  blueUpper?: number | null
  yellowLower?: number | null
  yellowUpper?: number | null
  redLower?: number | null
  redUpper?: number | null
  createdAt?: string
  updatedAt?: string
}

export type { ApiResponse }
