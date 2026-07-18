import { useState, type FormEvent } from 'react';
import { useAuth } from '../core/auth/AuthContext';
import { googleAuthProvider, naverAuthProvider, type SocialAuthProvider } from '../core/auth/socialAuth';
import { Button } from '../components/Button';
import './LoginPage.css';

type Mode = 'login' | 'register';

export function LoginPage() {
  const { login, loginWithEmail, registerWithEmail } = useAuth();
  const [mode, setMode] = useState<Mode>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const isEmailFormValid = email.trim().length > 0 && password.length >= 8;

  async function handleSocialLogin(provider: SocialAuthProvider) {
    setErrorMessage(null);
    setIsSubmitting(true);
    try {
      const token = await provider.signIn();
      await login(provider.provider, token);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '로그인에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleEmailSubmit(event: FormEvent) {
    event.preventDefault();
    if (!isEmailFormValid) {
      setErrorMessage('이메일과 8자 이상의 비밀번호를 입력해주세요.');
      return;
    }
    setErrorMessage(null);
    setIsSubmitting(true);
    try {
      if (mode === 'register') {
        await registerWithEmail(email, password);
      } else {
        await loginWithEmail(email, password);
      }
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '처리에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  }

  function toggleMode() {
    setErrorMessage(null);
    setMode((current) => (current === 'login' ? 'register' : 'login'));
  }

  return (
    <div className="login-page">
      <div className="login-page__intro">
        <span className="login-page__logo" aria-hidden>
          🌊
        </span>
        <h1>DiveApp</h1>
        <p>다이빙 기록, 훈련, 커뮤니티를 한 곳에서</p>
      </div>

      <div className="login-page__actions">
        {errorMessage && <p className="login-page__error">{errorMessage}</p>}

        <form onSubmit={handleEmailSubmit}>
          <label className="form-field">
            이메일
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
            />
          </label>
          <label className="form-field">
            비밀번호
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete={mode === 'register' ? 'new-password' : 'current-password'}
              minLength={8}
            />
          </label>
          <Button type="submit" disabled={!isEmailFormValid || isSubmitting}>
            {mode === 'register' ? '회원가입' : '로그인'}
          </Button>
        </form>

        <Button variant="text" onClick={toggleMode} disabled={isSubmitting}>
          {mode === 'register' ? '이미 계정이 있으신가요? 로그인' : '계정이 없으신가요? 회원가입'}
        </Button>

        <div className="login-page__divider">또는</div>

        <Button onClick={() => handleSocialLogin(naverAuthProvider)} disabled={isSubmitting}>
          네이버로 시작하기
        </Button>
        <Button variant="secondary" onClick={() => handleSocialLogin(googleAuthProvider)} disabled={isSubmitting}>
          구글로 시작하기
        </Button>
      </div>
    </div>
  );
}
