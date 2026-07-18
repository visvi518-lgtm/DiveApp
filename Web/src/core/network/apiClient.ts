import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL } from './apiConfig';
import { ApiError, type ServerErrorBody } from './apiError';

interface SessionHandlers {
  getAccessToken: () => string | null;
  refreshAccessToken: () => Promise<string>;
  onSessionExpired: () => void;
}

let sessionHandlers: SessionHandlers | null = null;

/** Registered once by AuthProvider on mount — lets the interceptor refresh
 * tokens and react to session expiry without a circular import. */
export function setSessionHandlers(handlers: SessionHandlers) {
  sessionHandlers = handlers;
}

declare module 'axios' {
  interface AxiosRequestConfig {
    noAuth?: boolean;
  }
  interface InternalAxiosRequestConfig {
    noAuth?: boolean;
    _retried?: boolean;
  }
}

export const apiClient = axios.create({ baseURL: API_BASE_URL });

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (!config.noAuth) {
    const token = sessionHandlers?.getAccessToken();
    if (token) {
      config.headers.set('Authorization', `Bearer ${token}`);
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig | undefined;

    if (error.response?.status === 401 && originalRequest && !originalRequest.noAuth && !originalRequest._retried) {
      originalRequest._retried = true;
      try {
        await sessionHandlers?.refreshAccessToken();
        return apiClient(originalRequest);
      } catch {
        sessionHandlers?.onSessionExpired();
        throw ApiError.unauthorized();
      }
    }

    if (error.response?.status === 401) {
      sessionHandlers?.onSessionExpired();
      throw ApiError.unauthorized();
    }

    if (!error.response) {
      throw ApiError.network();
    }

    const body = error.response.data as ServerErrorBody | undefined;
    if (body?.error) {
      throw ApiError.server(error.response.status, body.error.code, body.error.message);
    }
    throw ApiError.unknown();
  },
);
