import type { ButtonHTMLAttributes } from 'react';
import './Button.css';

type Variant = 'primary' | 'secondary' | 'destructive' | 'text';

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
}

/** Button roles from Docs/08_DesignSystem.md: Primary / Secondary / Destructive / Text. */
export function Button({ variant = 'primary', className, ...rest }: Props) {
  const classes = ['btn', `btn-${variant}`, className].filter(Boolean).join(' ');
  return <button className={classes} {...rest} />;
}
