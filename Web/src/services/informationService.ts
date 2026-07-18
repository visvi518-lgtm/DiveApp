import { apiClient } from '../core/network/apiClient';
import type {
  InformationArticleCreateRequest,
  InformationArticleListItem,
  InformationArticleResponse,
  InformationArticleUpdateRequest,
} from '../models/informationModels';

export const informationService = {
  async list(query?: string): Promise<InformationArticleListItem[]> {
    const { data } = await apiClient.get<InformationArticleListItem[]>('/api/v1/information/articles', {
      params: query ? { q: query } : undefined,
      noAuth: true,
    });
    return data;
  },
  async get(id: string): Promise<InformationArticleResponse> {
    const { data } = await apiClient.get<InformationArticleResponse>(`/api/v1/information/articles/${id}`, {
      noAuth: true,
    });
    return data;
  },
  /** Admin-only: unlike list(), includes unpublished articles. There is no
   * admin single-article GET on the backend, so edit pages must be handed the
   * full article (e.g. via router state) or fall back to searching this list. */
  async adminList(query?: string): Promise<InformationArticleResponse[]> {
    const { data } = await apiClient.get<InformationArticleResponse[]>('/api/v1/admin/information/articles', {
      params: query ? { q: query } : undefined,
    });
    return data;
  },
  async adminCreate(body: InformationArticleCreateRequest): Promise<InformationArticleResponse> {
    const { data } = await apiClient.post<InformationArticleResponse>('/api/v1/admin/information/articles', body);
    return data;
  },
  async adminUpdate(id: string, body: InformationArticleUpdateRequest): Promise<InformationArticleResponse> {
    const { data } = await apiClient.patch<InformationArticleResponse>(
      `/api/v1/admin/information/articles/${id}`,
      body,
    );
    return data;
  },
  async adminDelete(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/admin/information/articles/${id}`);
  },
};
