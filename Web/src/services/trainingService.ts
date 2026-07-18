import { apiClient } from '../core/network/apiClient';
import type { TrainingRecordCreateRequest, TrainingRecordResponse, TrainingStatisticsResponse } from '../models/trainingModels';

export const trainingService = {
  async create(body: TrainingRecordCreateRequest): Promise<TrainingRecordResponse> {
    const { data } = await apiClient.post<TrainingRecordResponse>('/api/v1/trainings', body);
    return data;
  },
  async list(): Promise<TrainingRecordResponse[]> {
    const { data } = await apiClient.get<TrainingRecordResponse[]>('/api/v1/trainings');
    return data;
  },
  async statistics(): Promise<TrainingStatisticsResponse> {
    const { data } = await apiClient.get<TrainingStatisticsResponse>('/api/v1/trainings/statistics');
    return data;
  },
};
