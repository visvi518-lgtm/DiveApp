import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { communityService } from '../../services/communityService';
import type { CommunityPostListItem } from '../../models/communityModels';
import { EmptyState, ErrorState, LoadingView } from '../../components/StateViews';
import '../../components/ListCard.css';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; posts: CommunityPostListItem[] }
  | { status: 'error'; message: string };

export function CommunityListPage() {
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    setState({ status: 'loading' });
    communityService
      .listPosts()
      .then((posts) => setState({ status: 'success', posts }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '게시글을 불러오지 못했습니다.' }),
      );
  }, []);

  useEffect(() => load(), [load]);

  return (
    <div>
      <div className="list-card__row" style={{ marginBottom: 'var(--space-lg)' }}>
        <h2>커뮤니티</h2>
        <Link to="/community/new" className="btn btn-primary" style={{ width: 'auto' }}>
          + 글쓰기
        </Link>
      </div>

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && state.posts.length === 0 && (
        <EmptyState title="등록된 게시글이 없습니다" message="오른쪽 위 + 버튼으로 첫 글을 남겨보세요." />
      )}
      {state.status === 'success' &&
        state.posts.map((post) => (
          <Link to={`/community/${post.id}`} key={post.id} className="list-card">
            <div className="list-card__title">{post.title}</div>
            <div className="list-card__row" style={{ marginTop: 'var(--space-xs)' }}>
              <span className="list-card__subtitle">{post.author.nickname ?? '알 수 없음'}</span>
              <span className="list-card__subtitle">
                조회 {post.view_count} · 댓글 {post.comment_count}
              </span>
            </div>
          </Link>
        ))}
    </div>
  );
}
