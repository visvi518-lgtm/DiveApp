import type { ReactNode } from 'react';
import './SubPageHeader.css';

/** Consistent header for non-root pages (detail/create/edit), with an
 * optional back link and trailing action slot. */
export function SubPageHeader({
  title,
  onBack,
  backLabel = '뒤로',
  action,
}: {
  title: string;
  onBack?: () => void;
  backLabel?: string;
  action?: ReactNode;
}) {
  return (
    <div className="sub-page-header">
      {onBack && (
        <button className="sub-page-header__back" onClick={onBack}>
          ← {backLabel}
        </button>
      )}
      <h2 className="sub-page-header__title">{title}</h2>
      {action && <div className="sub-page-header__action">{action}</div>}
    </div>
  );
}
