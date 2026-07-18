import { Link } from 'react-router-dom';
import { useAuth } from '../../core/auth/AuthContext';
import { Button } from '../../components/Button';
import '../../components/ListCard.css';

export function MyPagePage() {
  const { currentUser, logout } = useAuth();

  return (
    <div>
      <h2>{currentUser?.profile?.nickname ?? '닉네임 없음'}</h2>
      {currentUser?.email && <p style={{ color: 'var(--color-text-secondary)' }}>{currentUser.email}</p>}

      <Link
        to="/mypage/certificates"
        className="list-card"
        style={{ display: 'block', marginTop: 'var(--space-lg)', marginBottom: 'var(--space-xl)' }}
      >
        <div className="list-card__title">자격증 관리</div>
      </Link>

      <Button variant="text" onClick={() => void logout()} style={{ color: 'var(--color-error)' }}>
        로그아웃
      </Button>
    </div>
  );
}
