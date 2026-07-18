import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../core/auth/AuthContext';
import './AppLayout.css';

const NAV_ITEMS: { to: string; label: string; end?: boolean }[] = [
  { to: '/', label: '홈', end: true },
  { to: '/dive-logs', label: '다이브 로그' },
  { to: '/training', label: 'CO₂ Table' },
  { to: '/community', label: '커뮤니티' },
  { to: '/mypage', label: '마이페이지' },
];

const ADMIN_NAV_ITEM: { to: string; label: string; end?: boolean } = { to: '/admin', label: '관리자' };

/** Top-level layout: nav + routed content (Docs/03_UserFlow.md Home / Dive
 * Log / CO2 Table / Community / My Page). Each section owns its own nested
 * routes for list/detail/create/edit, mirroring the mobile apps' tab
 * structure. The Admin nav item only shows for ADMIN-role users. */
export function AppLayout() {
  const { currentUser } = useAuth();
  const navItems = currentUser?.role === 'ADMIN' ? [...NAV_ITEMS, ADMIN_NAV_ITEM] : NAV_ITEMS;

  return (
    <div className="app-layout">
      <header className="app-layout__header">
        <span className="app-layout__brand">DiveApp</span>
        <nav className="app-layout__nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `app-layout__nav-link${isActive ? ' app-layout__nav-link--active' : ''}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </header>
      <main className="app-layout__content">
        <Outlet />
      </main>
    </div>
  );
}
