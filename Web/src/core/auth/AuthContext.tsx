import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { setSessionHandlers } from '../network/apiClient';
import { ApiError } from '../network/apiError';
import { authService } from '../../services/authService';
import { userService } from '../../services/userService';
import { tokenStorage } from './tokenStorage';
import type { CurrentUser } from '../../models/userModels';
import type { AuthProvider } from '../../models/enums';
import type { TokenResponse } from '../../models/authModels';

/** Single source of truth for auth state, observed by the router to switch
 * between Login / Profile Setup / the main app (Docs/03_UserFlow.md
 * Authentication flow). Mirrors AuthSession on Android/iOS. */
export type AuthState = 'bootstrapping' | 'unauthenticated' | 'needsProfileSetup' | 'authenticated';

interface AuthContextValue {
  state: AuthState;
  currentUser: CurrentUser | null;
  login: (provider: AuthProvider, token: string) => Promise<void>;
  registerWithEmail: (email: string, password: string) => Promise<void>;
  loginWithEmail: (email: string, password: string) => Promise<void>;
  completeProfileSetup: () => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>('bootstrapping');
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);

  const loadCurrentUser = useCallback(async () => {
    const user = await userService.fetchCurrentUser();
    setCurrentUser(user);
    setState(user.profile == null ? 'needsProfileSetup' : 'authenticated');
  }, []);

  useEffect(() => {
    setSessionHandlers({
      getAccessToken: () => tokenStorage.accessToken,
      refreshAccessToken: async () => {
        const refreshToken = tokenStorage.refreshToken;
        if (!refreshToken) throw ApiError.unauthorized();
        const response = await authService.refresh(refreshToken);
        tokenStorage.accessToken = response.access_token;
        return response.access_token;
      },
      onSessionExpired: () => {
        tokenStorage.clear();
        setCurrentUser(null);
        setState('unauthenticated');
      },
    });

    async function bootstrap() {
      const refreshToken = tokenStorage.refreshToken;
      if (!refreshToken) {
        setState('unauthenticated');
        return;
      }
      try {
        const response = await authService.refresh(refreshToken);
        tokenStorage.accessToken = response.access_token;
        await loadCurrentUser();
      } catch {
        tokenStorage.clear();
        setState('unauthenticated');
      }
    }
    void bootstrap();
  }, [loadCurrentUser]);

  const applyTokenResponse = useCallback(
    async (response: TokenResponse) => {
      tokenStorage.save(response.access_token, response.refresh_token);
      if (response.is_new_user) {
        setState('needsProfileSetup');
      } else {
        await loadCurrentUser();
      }
    },
    [loadCurrentUser],
  );

  const login = useCallback(
    async (provider: AuthProvider, token: string) => {
      await applyTokenResponse(await authService.login(provider, token));
    },
    [applyTokenResponse],
  );

  const registerWithEmail = useCallback(
    async (email: string, password: string) => {
      await applyTokenResponse(await authService.register(email, password));
    },
    [applyTokenResponse],
  );

  const loginWithEmail = useCallback(
    async (email: string, password: string) => {
      await applyTokenResponse(await authService.loginWithEmail(email, password));
    },
    [applyTokenResponse],
  );

  const completeProfileSetup = useCallback(async () => {
    await loadCurrentUser();
  }, [loadCurrentUser]);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      // best-effort: proceed to clear local session even if the API call fails
    }
    tokenStorage.clear();
    setCurrentUser(null);
    setState('unauthenticated');
  }, []);

  return (
    <AuthContext.Provider
      value={{ state, currentUser, login, registerWithEmail, loginWithEmail, completeProfileSetup, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
