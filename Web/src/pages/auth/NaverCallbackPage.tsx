import { useEffect } from 'react';
import { createNaverLoginForCallback } from '../../core/auth/socialAuth';

/** Rendered inside the Naver login popup window after the user approves the
 * OAuth consent screen (this route must exactly match the callbackUrl
 * registered in Naver Developers). It reads the resulting access token from
 * the SDK and hands it back to the window that opened the popup, then closes
 * itself — the opener (socialAuth.ts::naverAuthProvider) is the one actually
 * resolving the login promise. */
export function NaverCallbackPage() {
  useEffect(() => {
    let cancelled = false;

    function reportToOpener(message: { accessToken?: string; error?: string }) {
      if (!window.opener) return;
      window.opener.postMessage({ source: 'diveapp-naver-login', ...message }, window.location.origin);
      window.close();
    }

    async function run() {
      try {
        const naverLogin = await createNaverLoginForCallback();
        naverLogin.getLoginStatus((status) => {
          if (cancelled) return;
          if (status && naverLogin.accessToken?.accessToken) {
            reportToOpener({ accessToken: naverLogin.accessToken.accessToken });
          } else {
            reportToOpener({ error: 'Naver 로그인이 완료되지 않았습니다.' });
          }
        });
      } catch (error) {
        if (!cancelled) {
          reportToOpener({ error: error instanceof Error ? error.message : 'Naver 로그인에 실패했습니다.' });
        }
      }
    }
    void run();

    return () => {
      cancelled = true;
    };
  }, []);

  return <p style={{ padding: 24, textAlign: 'center' }}>로그인 처리 중입니다...</p>;
}
