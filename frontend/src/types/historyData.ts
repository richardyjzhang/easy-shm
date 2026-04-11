import type { ApiResponse } from './department'

export interface StatsRecord {
  ts: string
  firstVal: number
  avgVal: number
  maxVal: number
  minVal: number
  stdVal: number
  countVal: number
}

export interface PointStatsData {
  pointId: number
  pointCode: string
  pointLocation: string
  records: StatsRecord[]
}

export type { ApiResponse }
