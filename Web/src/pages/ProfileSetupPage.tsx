import { useState } from 'react';
import { useAuth } from '../core/auth/AuthContext';
import { userService } from '../services/userService';
import { Button } from '../components/Button';
import './ProfileSetupPage.css';

export function ProfileSetupPage() {
  const { completeProfileSetup } = useAuth();
  const [nickname, setNickname] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const isValid = nickname.length >= 2 && nickname.length <= 30;

  async function handleSave() {
    if (!isValid) {
      setErrorMessage('닉네임은 2자 이상 30자 이하로 입력해주세요.');
      return;
    }
    setErrorMessage(null);
    setIsSaving(true);
    try {
      await userService.setupProfile({ nickname });
      await completeProfileSetup();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '프로필 저장에 실패했습니다.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <div className="profile-setup-page">
      <h1>프로필을 설정해주세요</h1>
      <p className="profile-setup-page__hint">닉네임은 나중에 마이페이지에서 변경할 수 있어요.</p>

      <input
        className="profile-setup-page__input"
        value={nickname}
        onChange={(event) => setNickname(event.target.value)}
        placeholder="닉네임 (2~30자)"
        maxLength={30}
      />

      {errorMessage && <p className="profile-setup-page__error">{errorMessage}</p>}

      <Button onClick={handleSave} disabled={!isValid || isSaving} style={{ marginTop: 'var(--space-lg)' }}>
        시작하기
      </Button>
    </div>
  );
}
