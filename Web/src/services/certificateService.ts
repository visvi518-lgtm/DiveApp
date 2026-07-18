import { apiClient } from '../core/network/apiClient';
import type { CertificateInput, CertificateResponse } from '../models/certificateModels';

export const certificateService = {
  async create(body: CertificateInput): Promise<CertificateResponse> {
    const { data } = await apiClient.post<CertificateResponse>('/api/v1/certificates', body);
    return data;
  },
  async list(): Promise<CertificateResponse[]> {
    const { data } = await apiClient.get<CertificateResponse[]>('/api/v1/certificates');
    return data;
  },
  async get(id: string): Promise<CertificateResponse> {
    const { data } = await apiClient.get<CertificateResponse>(`/api/v1/certificates/${id}`);
    return data;
  },
  async update(id: string, body: CertificateInput): Promise<CertificateResponse> {
    const { data } = await apiClient.patch<CertificateResponse>(`/api/v1/certificates/${id}`, body);
    return data;
  },
  async remove(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/certificates/${id}`);
  },
};
