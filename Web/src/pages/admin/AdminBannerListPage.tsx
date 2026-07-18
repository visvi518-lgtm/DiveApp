import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { bannerService } from '../../services/bannerService';
import type { BannerResponse } from '../../models/bannerModels';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { EmptyState, ErrorState, LoadingView } from '../../components/StateViews';
import '../../components/ListCard.css';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; banners: BannerResponse[] }
  | { status: 'error'; message: string };

export function AdminBannerListPage() {
  const navigate = useNavigate();
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    setState({ status: 'loading' });
    bannerService
      .adminList()
      .then((banners) => setState({ status: 'success', banners }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '배너 목록을 불러오지 못했습니다.' }),
      );
  }, []);

  useEffect(() => load(), [load]);

  async function handleDelete(id: string) {
    if (!confirm('배너를 삭제할까요? 삭제한 배너는 복구할 수 없습니다.')) return;
    try {
      await bannerService.adminDelete(id);
      load();
    } catch (error) {
      alert(error instanceof Error ? error.message : '삭제에 실패했습니다.');
    }
  }

  return (
    <div>
      <SubPageHeader
        title="배너 관리"
        onBack={() => navigate('/admin')}
        backLabel="대시보드"
        action={
          <Button onClick={() => navigate('/admin/banners/new')} style={{ width: 'auto' }}>
            + 배너 추가
          </Button>
        }
      />

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && state.banners.length === 0 && <EmptyState title="등록된 배너가 없습니다" />}
      {state.status === 'success' &&
        state.banners
          .slice()
          .sort((a, b) => a.display_order - b.display_order)
          .map((banner) => (
            <div key={banner.id} className="list-card">
              <div className="list-card__row">
                <div>
                  <div className="list-card__title">{banner.title}</div>
                  <div className="list-card__subtitle">
                    순서 {banner.display_order} · {banner.is_active ? '노출 중' : '비노출'}
                  </div>
                </div>
                <div style={{ display: 'flex', gap: 'var(--space-sm)' }}>
                  <Button variant="text" onClick={() => navigate(`/admin/banners/${banner.id}/edit`, { state: banner })}>
                    수정
                  </Button>
                  <Button variant="text" onClick={() => handleDelete(banner.id)} style={{ color: 'var(--color-error)' }}>
                    삭제
                  </Button>
                </div>
              </div>
            </div>
          ))}
    </div>
  );
}
