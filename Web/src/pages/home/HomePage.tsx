import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useAuth } from '../../core/auth/AuthContext';
import { diveLogService } from '../../services/diveLogService';
import type { DiveLogStatisticsResponse } from '../../models/diveLogModels';
import './HomePage.css';

const FEATURE_LINKS = [
  { to: '/dive-logs', icon: '🤿', title: '다이브 로그', description: '나의 다이빙 기록을 남겨보세요' },
  { to: '/training', icon: '⏱️', title: 'CO₂ Table', description: '프리다이빙 훈련 타이머' },
  { to: '/community', icon: '💬', title: '커뮤니티', description: '다이버들과 소통해보세요' },
  { to: '/information', icon: '📖', title: '정보 게시판', description: '프리다이빙 · 스쿠버다이빙 소식' },
];

export function HomePage() {
  const { currentUser } = useAuth();
  const nickname = currentUser?.profile?.nickname;
  const [stats, setStats] = useState<DiveLogStatisticsResponse | null>(null);

  useEffect(() => {
    diveLogService
      .statistics()
      .then(setStats)
      .catch(() => setStats(null));
  }, []);

  return (
    <div className="home-page">
      <div className="home-page__hero">
        <span className="home-page__hero-icon" aria-hidden>
          🌊
        </span>
        <h1>{nickname ? `${nickname}님, 안녕하세요!` : '안녕하세요!'}</h1>
        <p>오늘도 안전한 다이빙 되세요.</p>

        {stats != null && stats.total_dive_count > 0 && (
          <div className="home-page__stats">
            <div className="home-page__stat">
              <strong>{stats.total_dive_count}</strong>
              <span>총 다이빙</span>
            </div>
            <div className="home-page__stat-divider" aria-hidden />
            <div className="home-page__stat">
              <strong>{stats.max_depth_overall}m</strong>
              <span>최대 수심</span>
            </div>
          </div>
        )}
      </div>

      <div className="home-page__banner">
        <span className="home-page__banner-icon" aria-hidden>
          📢
        </span>
        <div>
          <strong>배너</strong>
          <p>배너 노출 기능은 추후 제공될 예정입니다.</p>
        </div>
      </div>

      <div className="home-page__grid">
        {FEATURE_LINKS.map((feature) => (
          <Link to={feature.to} key={feature.to} className="home-page__feature-card">
            <span className="home-page__feature-icon" aria-hidden>
              {feature.icon}
            </span>
            <h3>{feature.title}</h3>
            <p>{feature.description}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
