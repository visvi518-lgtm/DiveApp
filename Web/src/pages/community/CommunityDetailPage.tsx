import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { communityService } from '../../services/communityService';
import type { CommunityCommentResponse, CommunityPostResponse } from '../../models/communityModels';
import { useAuth } from '../../core/auth/AuthContext';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { ErrorState, LoadingView } from '../../components/StateViews';

type UiState = { status: 'loading' } | { status: 'success'; post: CommunityPostResponse } | { status: 'error'; message: string };

export function CommunityDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { currentUser } = useAuth();

  const [state, setState] = useState<UiState>({ status: 'loading' });
  const [comments, setComments] = useState<CommunityCommentResponse[]>([]);
  const [newComment, setNewComment] = useState('');
  const [isPostingComment, setIsPostingComment] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const load = useCallback(() => {
    if (!id) return;
    setState({ status: 'loading' });
    Promise.all([communityService.getPost(id), communityService.listComments(id)])
      .then(([post, commentList]) => {
        setState({ status: 'success', post });
        setComments(commentList);
      })
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '게시글을 불러오지 못했습니다.' }),
      );
  }, [id]);

  useEffect(() => load(), [load]);

  async function handlePostComment() {
    if (!id || !newComment.trim()) return;
    setIsPostingComment(true);
    setActionError(null);
    try {
      await communityService.createComment(id, newComment);
      setNewComment('');
      setComments(await communityService.listComments(id));
    } catch (error) {
      setActionError(error instanceof Error ? error.message : '댓글 작성에 실패했습니다.');
    } finally {
      setIsPostingComment(false);
    }
  }

  async function handleDeleteComment(commentId: string) {
    if (!id) return;
    try {
      await communityService.deleteComment(commentId);
      setComments(await communityService.listComments(id));
    } catch (error) {
      setActionError(error instanceof Error ? error.message : '댓글 삭제에 실패했습니다.');
    }
  }

  async function handleDeletePost() {
    if (!id || !confirm('게시글을 삭제할까요? 삭제한 게시글은 복구할 수 없습니다.')) return;
    await communityService.deletePost(id);
    navigate('/community');
  }

  return (
    <div>
      <SubPageHeader title="게시글" onBack={() => navigate('/community')} backLabel="목록" />

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && (
        <>
          <h2>{state.post.title}</h2>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.8125rem', marginTop: 'var(--space-xs)' }}>
            {state.post.author.nickname ?? '알 수 없음'} · 조회 {state.post.view_count}
          </p>
          <p style={{ marginTop: 'var(--space-lg)', whiteSpace: 'pre-wrap' }}>{state.post.content}</p>

          {currentUser?.id === state.post.author.id && (
            <div style={{ display: 'flex', gap: 'var(--space-sm)', marginTop: 'var(--space-md)' }}>
              <Button variant="text" onClick={() => navigate(`/community/${id}/edit`)}>
                수정
              </Button>
              <Button variant="text" onClick={handleDeletePost} style={{ color: 'var(--color-error)' }}>
                삭제
              </Button>
            </div>
          )}

          <hr style={{ margin: 'var(--space-xl) 0', border: 'none', borderTop: '1px solid var(--color-border)' }} />

          <h3>댓글 {comments.length}</h3>
          {comments.map((comment) => (
            <div key={comment.id} className="list-card__row" style={{ marginTop: 'var(--space-md)' }}>
              <div>
                <div style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)' }}>
                  {comment.author.nickname ?? '알 수 없음'}
                </div>
                <div>{comment.content}</div>
              </div>
              {currentUser?.id === comment.author.id && (
                <Button variant="text" onClick={() => handleDeleteComment(comment.id)} style={{ color: 'var(--color-error)' }}>
                  삭제
                </Button>
              )}
            </div>
          ))}

          <div style={{ display: 'flex', gap: 'var(--space-sm)', marginTop: 'var(--space-lg)' }}>
            <input
              value={newComment}
              onChange={(event) => setNewComment(event.target.value)}
              placeholder="댓글 작성"
              style={{
                flex: 1,
                padding: 'var(--space-md)',
                border: '1px solid var(--color-border)',
                borderRadius: 'var(--radius-md)',
                background: 'var(--color-background)',
                color: 'var(--color-text)',
              }}
            />
            <Button
              variant="secondary"
              onClick={handlePostComment}
              disabled={!newComment.trim() || isPostingComment}
              style={{ width: 'auto' }}
            >
              등록
            </Button>
          </div>
          {actionError && <p className="form-error">{actionError}</p>}
        </>
      )}
    </div>
  );
}
