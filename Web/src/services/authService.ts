import { apiClient } from '../core/network/apiClient';
import type {
  AccessTokenResponse,
  EmailLoginRequest,
  EmailRegisterRequest,
  RefreshTokenRequest,
  SocialLoginRequest,
  TokenResponse,
} from '../models/authModels';
import type { AuthProvider } from '../models/enums';

export const authService = {
  async login(provider: AuthProvider, token: string): Promise<TokenResponse> {
    const body: SocialLoginRequest = { token };
    const { data } = await apiClient.post<TokenResponse>(`/api/v1/auth/login/${provider}`, body, { noAuth: true });
    return data;
  },

  async register(email: string, password: string): Promise<TokenResponse> {
    const body: EmailRegisterRequest = { email, password };
    const { data } = await apiClient.post<TokenResponse>('/api/v1/auth/register', body, { noAuth: true });
    return data;
  },

  async loginWithEmail(email: string, password: string): Promise<TokenResponse> {
    const body: EmailLoginRequest = { email, password };
    const { data } = await apiClient.post<TokenResponse>('/api/v1/auth/login/email', body, { noAuth: true });
    return data;
  },

  async refresh(refreshToken: string): Promise<AccessTokenResponse> {
    const body: RefreshTokenRequest = { refresh_token: refreshToken };
    const { data } = await apiClient.post<AccessTokenResponse>('/api/v1/auth/refresh', body, { noAuth: true });
    return data;
  },

  async logout(): Promise<void> {
    await apiClient.post('/api/v1/auth/logout');
  },
};
