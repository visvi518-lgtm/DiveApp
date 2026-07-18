/** Persists the access/refresh token pair in localStorage across page loads.
 * A browser SPA has no Keychain/Keystore equivalent; localStorage is the
 * pragmatic choice given the backend only supports Bearer tokens (no
 * httpOnly-cookie session support). */
const ACCESS_TOKEN_KEY = 'diveapp.accessToken';
const REFRESH_TOKEN_KEY = 'diveapp.refreshToken';

export const tokenStorage = {
  get accessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  },
  set accessToken(value: string | null) {
    if (value) localStorage.setItem(ACCESS_TOKEN_KEY, value);
    else localStorage.removeItem(ACCESS_TOKEN_KEY);
  },
  get refreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  },
  set refreshToken(value: string | null) {
    if (value) localStorage.setItem(REFRESH_TOKEN_KEY, value);
    else localStorage.removeItem(REFRESH_TOKEN_KEY);
  },
  save(accessToken: string, refreshToken: string) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  },
  clear() {
    this.accessToken = null;
    this.refreshToken = null;
  },
};
