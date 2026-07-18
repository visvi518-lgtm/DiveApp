import type { AuthProvider } from '../../models/enums';

export class SocialAuthError extends Error {
  readonly provider: AuthProvider;

  constructor(provider: AuthProvider, message?: string) {
    super(message ?? `${provider} 로그인에 실패했습니다.`);
    this.provider = provider;
  }
}

export interface SocialAuthProvider {
  provider: AuthProvider;
  signIn(): Promise<string>;
}

const SIGN_IN_TIMEOUT_MS = 120_000;

function withTimeout<T>(promise: Promise<T>, provider: AuthProvider): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const timer = setTimeout(
      () => reject(new SocialAuthError(provider, '로그인 시간이 초과되었습니다. 다시 시도해주세요.')),
      SIGN_IN_TIMEOUT_MS,
    );
    promise.then(
      (value) => {
        clearTimeout(timer);
        resolve(value);
      },
      (error) => {
        clearTimeout(timer);
        reject(error);
      },
    );
  });
}

const scriptLoadPromises = new Map<string, Promise<void>>();

function loadScript(src: string): Promise<void> {
  const cached = scriptLoadPromises.get(src);
  if (cached) return cached;

  const promise = new Promise<void>((resolve, reject) => {
    const script = document.createElement('script');
    script.src = src;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error(`스크립트를 불러오지 못했습니다: ${src}`));
    document.head.appendChild(script);
  });
  scriptLoadPromises.set(src, promise);
  return promise;
}

function createHiddenContainer(id?: string): HTMLDivElement {
  const container = document.createElement('div');
  if (id) container.id = id;
  container.style.position = 'fixed';
  container.style.top = '-9999px';
  container.style.left = '-9999px';
  document.body.appendChild(container);
  return container;
}

// ---------------------------------------------------------------------------
// Google Identity Services
//
// The backend verifies a Google **ID token** (a JWT) against GOOGLE_CLIENT_ID
// (see Backend/app/services/oauth_service.py::_verify_google_token), so the
// client must obtain an ID token, not an access token. Google's GSI library
// only issues one from its own rendered button, so we render that button into
// an off-screen container and forward clicks from our styled button to it —
// this is the standard workaround since GSI has no public "trigger sign-in
// programmatically" API for a custom UI.
// ---------------------------------------------------------------------------

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize(config: {
            client_id: string;
            callback: (response: { credential: string }) => void;
          }): void;
          renderButton(parent: HTMLElement, options: { type: 'icon' | 'standard'; width?: number }): void;
        };
      };
    };
  }
}

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID as string | undefined;
const GOOGLE_SDK_URL = 'https://accounts.google.com/gsi/client';

let googleButtonContainer: HTMLDivElement | null = null;

async function renderGoogleButtonAndGetIdToken(): Promise<string> {
  if (!GOOGLE_CLIENT_ID) {
    throw new SocialAuthError('GOOGLE', 'Google 로그인이 설정되지 않았습니다 (VITE_GOOGLE_CLIENT_ID 누락).');
  }
  await loadScript(GOOGLE_SDK_URL);
  if (!window.google) {
    throw new SocialAuthError('GOOGLE', 'Google 로그인 SDK를 불러오지 못했습니다.');
  }

  return new Promise<string>((resolve, reject) => {
    window.google!.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID!,
      callback: (response) => resolve(response.credential),
    });

    if (!googleButtonContainer) {
      googleButtonContainer = createHiddenContainer();
    }
    googleButtonContainer.innerHTML = '';
    window.google!.accounts.id.renderButton(googleButtonContainer, { type: 'icon', width: 200 });

    const realButton = googleButtonContainer.querySelector<HTMLElement>('div[role="button"]');
    if (!realButton) {
      reject(new SocialAuthError('GOOGLE', 'Google 로그인 버튼을 렌더링하지 못했습니다.'));
      return;
    }
    realButton.click();
  });
}

export const googleAuthProvider: SocialAuthProvider = {
  provider: 'GOOGLE',
  signIn(): Promise<string> {
    return withTimeout(renderGoogleButtonAndGetIdToken(), 'GOOGLE');
  },
};

// ---------------------------------------------------------------------------
// Naver Login (JS SDK)
//
// The backend verifies a Naver **access token** directly against Naver's own
// userinfo endpoint (see Backend/app/services/oauth_service.py::_verify_naver_token),
// so the client just needs a Naver access token. The SDK's own widget must be
// rendered (it injects its own <a> into a required #naverIdLogin element) and
// clicked to open the login popup; after the popup completes at our
// registered callback route (NaverCallbackPage), that page posts the access
// token back to this window via postMessage.
// ---------------------------------------------------------------------------

export interface NaverLoginInstance {
  init(): void;
  getLoginStatus(callback: (status: boolean) => void): void;
  accessToken?: { accessToken: string };
}

declare global {
  interface Window {
    naver?: {
      LoginWithNaverId: new (options: {
        clientId: string;
        callbackUrl: string;
        isPopup: boolean;
        /** Required: the SDK only injects its <a> login button into
         * #naverIdLogin when this option is present (its internal
         * `createButtonElement` no-ops otherwise, silently). */
        loginButton: { color: 'green' | 'white'; type: 1 | 2 | 3; height: number };
      }) => NaverLoginInstance;
    };
  }
}

const NAVER_CLIENT_ID = import.meta.env.VITE_NAVER_CLIENT_ID as string | undefined;
const NAVER_SDK_URL = 'https://static.nid.naver.com/js/naveridlogin_js_sdk_2.0.0.js';
const NAVER_LOGIN_CONTAINER_ID = 'naverIdLogin';
const NAVER_LOGIN_BUTTON_OPTION = { color: 'green', type: 3, height: 60 } as const;

export function naverCallbackUrl(): string {
  return (import.meta.env.VITE_NAVER_CALLBACK_URL as string | undefined) ?? `${window.location.origin}/auth/naver/callback`;
}

interface NaverPopupMessage {
  source: 'diveapp-naver-login';
  accessToken?: string;
  error?: string;
}

function isNaverPopupMessage(data: unknown): data is NaverPopupMessage {
  return typeof data === 'object' && data !== null && (data as { source?: unknown }).source === 'diveapp-naver-login';
}

async function openNaverPopupAndGetAccessToken(): Promise<string> {
  if (!NAVER_CLIENT_ID) {
    throw new SocialAuthError('NAVER', 'Naver 로그인이 설정되지 않았습니다 (VITE_NAVER_CLIENT_ID 누락).');
  }
  await loadScript(NAVER_SDK_URL);
  if (!window.naver) {
    throw new SocialAuthError('NAVER', 'Naver 로그인 SDK를 불러오지 못했습니다.');
  }

  let container = document.getElementById(NAVER_LOGIN_CONTAINER_ID) as HTMLDivElement | null;
  if (!container) {
    container = createHiddenContainer(NAVER_LOGIN_CONTAINER_ID);
  }
  container.innerHTML = '';

  const naverLogin = new window.naver.LoginWithNaverId({
    clientId: NAVER_CLIENT_ID,
    callbackUrl: naverCallbackUrl(),
    isPopup: true,
    loginButton: NAVER_LOGIN_BUTTON_OPTION,
  });
  naverLogin.init();

  return new Promise<string>((resolve, reject) => {
    function handleMessage(event: MessageEvent) {
      if (event.origin !== window.location.origin || !isNaverPopupMessage(event.data)) return;
      window.removeEventListener('message', handleMessage);
      if (event.data.accessToken) {
        resolve(event.data.accessToken);
      } else {
        reject(new SocialAuthError('NAVER', event.data.error ?? 'Naver 로그인에 실패했습니다.'));
      }
    }
    window.addEventListener('message', handleMessage);

    const realButton = container!.querySelector<HTMLElement>('a');
    if (!realButton) {
      window.removeEventListener('message', handleMessage);
      reject(new SocialAuthError('NAVER', 'Naver 로그인 버튼을 렌더링하지 못했습니다.'));
      return;
    }
    realButton.click();
  });
}

export const naverAuthProvider: SocialAuthProvider = {
  provider: 'NAVER',
  signIn(): Promise<string> {
    return withTimeout(openNaverPopupAndGetAccessToken(), 'NAVER');
  },
};

/** Used only by NaverCallbackPage, which runs inside the login popup window
 * and needs the same SDK instance/config to read the resulting access token.
 *
 * Calling `.init()` here is required, not optional: it's what makes the SDK
 * parse the OAuth response (access_token/state) out of the current page's
 * URL hash and populate `accessToken`/`loginStatus` — without it,
 * `getLoginStatus()` always reports a failed login regardless of whether the
 * user actually completed the real Naver consent screen. `.init()` also
 * calls `setLoginButton()` internally, which expects a `#naverIdLogin`
 * element to already exist in the DOM (it's unused here since nothing is
 * ever clicked on this page, but it must exist or `.init()` throws). */
export async function createNaverLoginForCallback(): Promise<NaverLoginInstance> {
  if (!NAVER_CLIENT_ID) {
    throw new SocialAuthError('NAVER', 'Naver 로그인이 설정되지 않았습니다 (VITE_NAVER_CLIENT_ID 누락).');
  }
  await loadScript(NAVER_SDK_URL);
  if (!window.naver) {
    throw new SocialAuthError('NAVER', 'Naver 로그인 SDK를 불러오지 못했습니다.');
  }
  if (!document.getElementById(NAVER_LOGIN_CONTAINER_ID)) {
    createHiddenContainer(NAVER_LOGIN_CONTAINER_ID);
  }
  const naverLogin = new window.naver.LoginWithNaverId({
    clientId: NAVER_CLIENT_ID,
    callbackUrl: naverCallbackUrl(),
    isPopup: true,
    loginButton: NAVER_LOGIN_BUTTON_OPTION,
  });
  naverLogin.init();
  return naverLogin;
}
