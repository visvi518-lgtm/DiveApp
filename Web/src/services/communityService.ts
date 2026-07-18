import { apiClient } from '../core/network/apiClient';
import type {
  CommunityCommentResponse,
  CommunityPostCreateRequest,
  CommunityPostListItem,
  CommunityPostResponse,
} from '../models/communityModels';

export const communityService = {
  async createPost(body: CommunityPostCreateRequest): Promise<CommunityPostResponse> {
    const { data } = await apiClient.post<CommunityPostResponse>('/api/v1/community/posts', body);
    return data;
  },
  async listPosts(query?: string): Promise<CommunityPostListItem[]> {
    const { data } = await apiClient.get<CommunityPostListItem[]>('/api/v1/community/posts', {
      params: query ? { q: query } : undefined,
      noAuth: true,
    });
    return data;
  },
  async getPost(id: string): Promise<CommunityPostResponse> {
    const { data } = await apiClient.get<CommunityPostResponse>(`/api/v1/community/posts/${id}`, { noAuth: true });
    return data;
  },
  async updatePost(id: string, body: CommunityPostCreateRequest): Promise<CommunityPostResponse> {
    const { data } = await apiClient.patch<CommunityPostResponse>(`/api/v1/community/posts/${id}`, body);
    return data;
  },
  async deletePost(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/community/posts/${id}`);
  },
  async createComment(postId: string, content: string): Promise<CommunityCommentResponse> {
    const { data } = await apiClient.post<CommunityCommentResponse>(`/api/v1/community/posts/${postId}/comments`, {
      content,
    });
    return data;
  },
  async listComments(postId: string): Promise<CommunityCommentResponse[]> {
    const { data } = await apiClient.get<CommunityCommentResponse[]>(`/api/v1/community/posts/${postId}/comments`, {
      noAuth: true,
    });
    return data;
  },
  async deleteComment(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/community/comments/${id}`);
  },
};
