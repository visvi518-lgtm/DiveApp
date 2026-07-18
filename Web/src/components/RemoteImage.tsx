import { useState } from 'react';
import './RemoteImage.css';

/** img wrapper with a placeholder/error fallback, per Docs/08_DesignSystem.md
 * "Images" section. */
export function RemoteImage({ src, alt = '', className }: { src?: string | null; alt?: string; className?: string }) {
  const [failed, setFailed] = useState(false);

  if (!src || failed) {
    return (
      <div className={['remote-image remote-image--placeholder', className].filter(Boolean).join(' ')}>
        <span aria-hidden>🖼</span>
      </div>
    );
  }

  return (
    <img
      src={src}
      alt={alt}
      className={['remote-image', className].filter(Boolean).join(' ')}
      onError={() => setFailed(true)}
    />
  );
}
