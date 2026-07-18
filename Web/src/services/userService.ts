import { apiClient } from '../core/network/apiClient';
import type { ProfileSetupRequest, CurrentUser, UserProfileResponse } from '../models/userModels';

export const userService = {
  async fetchCurrentUser(): Promise<CurrentUser> {
    const { data } = await apiClient.get<CurrentUser>('/api/v1/users/me');
    return data;
  },

  async setupProfile(body: ProfileSetupRequest): Promise<UserProfileResponse> {
    const { data } = await apiClient.post<UserProfileResponse>('/api/v1/users/me/profile', body);
    return data;
  },
};
