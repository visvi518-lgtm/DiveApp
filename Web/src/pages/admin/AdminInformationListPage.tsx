import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { informationService } from '../../services/informationService';
import type { InformationArticleResponse } from '../../models/informationModels';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { EmptyState, ErrorState, LoadingView } from '../../components/StateViews';
import '../../components/ListCard.css';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; articles: InformationArticleResponse[] }
  | { status: 'error'; message: string };

export function AdminInformationListPage() {
  const navigate = useNavigate();
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    setState({ status: 'loading' });
    informationService
      .adminList()
      .then((articles) => setState({ status: 'success', articles }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '정보글 목록을 불러오지 못했습니다.' }),
      );
  }, []);

  useEffect(() => load(), [load]);

  async function handleDelete(id: string) {
    if (!confirm('정보글을 삭제할까요? 삭제한 글은 복구할 수 없습니다.')) return;
    try {
      await informationService.adminDelete(id);
      load();
    } catch (error) {
      alert(error instanceof Error ? error.message : '삭제에 실패했습니다.');
    }
  }

  return (
    <div>
      <SubPageHeader
        title="정보 게시판 관리"
        onBack={() => navigate('/admin')}
        backLabel="대시보드"
        action={
          <Button onClick={() => navigate('/admin/information/new')} style={{ width: 'auto' }}>
            + 새 글쓰기
          </Button>
        }
      />

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && state.articles.length === 0 && <EmptyState title="등록된 정보글이 없습니다" />}
      {state.status === 'success' &&
        state.articles.map((article) => (
          <div key={article.id} className="list-card">
            <div className="list-card__row">
              <div>
                <div className="list-card__title">{article.title}</div>
                <div className="list-card__subtitle">
                  {article.is_published ? '발행됨' : '비공개'} · 조회 {article.view_count}
                </div>
              </div>
              <div style={{ display: 'flex', gap: 'var(--space-sm)' }}>
                <Button
                  variant="text"
                  onClick={() => navigate(`/admin/information/${article.id}/edit`, { state: article })}
                >
                  수정
                </Button>
                <Button variant="text" onClick={() => handleDelete(article.id)} style={{ color: 'var(--color-error)' }}>
                  삭제
                </Button>
              </div>
            </div>
          </div>
        ))}
    </div>
  );
}
