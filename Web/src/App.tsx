import type { ReactNode } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './core/auth/AuthContext';
import { LoadingView } from './components/StateViews';
import { LoginPage } from './pages/LoginPage';
import { NaverCallbackPage } from './pages/auth/NaverCallbackPage';
import { ProfileSetupPage } from './pages/ProfileSetupPage';
import { AppLayout } from './layouts/AppLayout';
import { HomePage } from './pages/home/HomePage';
import { InformationListPage } from './pages/information/InformationListPage';
import { InformationDetailPage } from './pages/information/InformationDetailPage';
import { DiveLogListPage } from './pages/divelog/DiveLogListPage';
import { DiveLogCreatePage } from './pages/divelog/DiveLogCreatePage';
import { DiveLogDetailPage } from './pages/divelog/DiveLogDetailPage';
import { DiveLogEditPage } from './pages/divelog/DiveLogEditPage';
import { TrainingPage } from './pages/training/TrainingPage';
import { TrainingHistoryPage } from './pages/training/TrainingHistoryPage';
import { CommunityListPage } from './pages/community/CommunityListPage';
import { CommunityFormPage } from './pages/community/CommunityFormPage';
import { CommunityDetailPage } from './pages/community/CommunityDetailPage';
import { MyPagePage } from './pages/mypage/MyPagePage';
import { CertificateListPage } from './pages/certificate/CertificateListPage';
import { CertificateFormPage } from './pages/certificate/CertificateFormPage';
import { CertificateDetailPage } from './pages/certificate/CertificateDetailPage';
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { AdminUserListPage } from './pages/admin/AdminUserListPage';
import { AdminUserDetailPage } from './pages/admin/AdminUserDetailPage';
import { AdminInformationListPage } from './pages/admin/AdminInformationListPage';
import { AdminInformationFormPage } from './pages/admin/AdminInformationFormPage';
import { AdminBannerListPage } from './pages/admin/AdminBannerListPage';
import { AdminBannerFormPage } from './pages/admin/AdminBannerFormPage';

function RequireAuth({ children }: { children: ReactNode }) {
  const { state } = useAuth();
  if (state === 'bootstrapping') return <LoadingView />;
  if (state === 'unauthenticated') return <Navigate to="/login" replace />;
  if (state === 'needsProfileSetup') return <Navigate to="/profile-setup" replace />;
  return <>{children}</>;
}

function RequireAdmin({ children }: { children: ReactNode }) {
  const { state, currentUser } = useAuth();
  if (state === 'bootstrapping') return <LoadingView />;
  if (state === 'unauthenticated') return <Navigate to="/login" replace />;
  if (state === 'needsProfileSetup') return <Navigate to="/profile-setup" replace />;
  if (currentUser?.role !== 'ADMIN') return <Navigate to="/" replace />;
  return <>{children}</>;
}

function PublicOnly({ children }: { children: ReactNode }) {
  const { state } = useAuth();
  if (state === 'bootstrapping') return <LoadingView />;
  if (state === 'authenticated') return <Navigate to="/" replace />;
  if (state === 'needsProfileSetup') return <Navigate to="/profile-setup" replace />;
  return <>{children}</>;
}

function RequireProfileSetup({ children }: { children: ReactNode }) {
  const { state } = useAuth();
  if (state === 'bootstrapping') return <LoadingView />;
  if (state === 'unauthenticated') return <Navigate to="/login" replace />;
  if (state === 'authenticated') return <Navigate to="/" replace />;
  return <>{children}</>;
}

function AppRoutes() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <PublicOnly>
            <LoginPage />
          </PublicOnly>
        }
      />
      <Route
        path="/profile-setup"
        element={
          <RequireProfileSetup>
            <ProfileSetupPage />
          </RequireProfileSetup>
        }
      />
      <Route path="/auth/naver/callback" element={<NaverCallbackPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <AppLayout />
          </RequireAuth>
        }
      >
        <Route index element={<HomePage />} />

        <Route path="information" element={<InformationListPage />} />
        <Route path="information/:id" element={<InformationDetailPage />} />

        <Route path="dive-logs" element={<DiveLogListPage />} />
        <Route path="dive-logs/new" element={<DiveLogCreatePage />} />
        <Route path="dive-logs/:id" element={<DiveLogDetailPage />} />
        <Route path="dive-logs/:id/edit" element={<DiveLogEditPage />} />

        <Route path="training" element={<TrainingPage />} />
        <Route path="training/history" element={<TrainingHistoryPage />} />

        <Route path="community" element={<CommunityListPage />} />
        <Route path="community/new" element={<CommunityFormPage />} />
        <Route path="community/:id" element={<CommunityDetailPage />} />
        <Route path="community/:id/edit" element={<CommunityFormPage />} />

        <Route path="mypage" element={<MyPagePage />} />
        <Route path="mypage/certificates" element={<CertificateListPage />} />
        <Route path="mypage/certificates/new" element={<CertificateFormPage />} />
        <Route path="mypage/certificates/:id" element={<CertificateDetailPage />} />
        <Route path="mypage/certificates/:id/edit" element={<CertificateFormPage />} />

        <Route
          path="admin"
          element={
            <RequireAdmin>
              <AdminDashboardPage />
            </RequireAdmin>
          }
        />
        <Route
          path="admin/users"
          element={
            <RequireAdmin>
              <AdminUserListPage />
            </RequireAdmin>
          }
        />
        <Route
          path="admin/users/:id"
          element={
            <RequireAdmin>
              <AdminUserDetailPage />
            </RequireAdmin>
          }
        />
        <Route
          path="admin/information"
          element={
            <RequireAdmin>
              <AdminInformationListPage />
            </RequireAdmin>
          }
        />
        <Route
          path="admin/information/new"
          element={
            <RequireAdmin>
              <AdminInformationFormPage />
            </RequireAdmin>
          }
        />
        <Route
          path="admin/information/:id/edit"
          element={
            <RequireAdmin>
              <AdminInformationFormPage />
            </RequireAdmin>
          }
        />
        <Route
          path="admin/banners"
          element={
            <RequireAdmin>
              <AdminBannerListPage />
            </RequireAdmin>
          }
        />
        <Route
          path="admin/banners/new"
          element={
            <RequireAdmin>
              <AdminBannerFormPage />
            </RequireAdmin>
          }
        />
        <Route
          path="admin/banners/:id/edit"
          element={
            <RequireAdmin>
              <AdminBannerFormPage />
            </RequireAdmin>
          }
        />
      </Route>
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
