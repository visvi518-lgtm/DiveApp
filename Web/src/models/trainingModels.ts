export interface TrainingRecordCreateRequest {
  total_sets: number;
  completed_sets: number;
  is_completed: boolean;
  rest_time_seconds: number;
  hold_time_seconds: number;
  rest_interval_seconds: number;
  hold_interval_seconds: number;
}

export interface TrainingRecordResponse extends TrainingRecordCreateRequest {
  id: string;
  completed_at: string;
}

export interface TrainingStatisticsResponse {
  total_training_count: number;
  completion_rate: number;
  average_completed_sets: number;
  last_training_at: string | null;
}
