import type { ReactNode } from 'react';
import { Button } from './Button';
import './StateViews.css';

/** Shared loading indicator so every screen renders the same waiting state. */
export function LoadingView({ message }: { message?: string }) {
  return (
    <div className="state-view">
      <div className="spinner" aria-hidden />
      {message && <p className="state-view__message">{message}</p>}
    </div>
  );
}

/** Shared empty state for list screens, per Docs/08_DesignSystem.md. */
export function EmptyState({ title, message, icon }: { title: string; message?: string; icon?: ReactNode }) {
  return (
    <div className="state-view">
      {icon && <div className="state-view__icon">{icon}</div>}
      <h3 className="state-view__title">{title}</h3>
      {message && <p className="state-view__message">{message}</p>}
    </div>
  );
}

/** Shared error state for failed network requests, per Docs/08_DesignSystem.md. */
export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="state-view">
      <div className="state-view__icon state-view__icon--error">⚠</div>
      <p className="state-view__message">{message}</p>
      {onRetry && (
        <Button variant="secondary" onClick={onRetry} style={{ width: 'auto', marginTop: 'var(--space-md)' }}>
          다시 시도
        </Button>
      )}
    </div>
  );
}
