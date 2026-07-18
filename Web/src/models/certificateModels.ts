import type { CertificationOrganization } from './enums';

export interface CertificateInput {
  organization: CertificationOrganization;
  certification_level: string;
  certification_number?: string | null;
  issue_date?: string | null;
  expiration_date?: string | null;
  instructor?: string | null;
  dive_center?: string | null;
  certificate_image_url?: string | null;
  memo?: string | null;
}

export interface CertificateResponse extends CertificateInput {
  id: string;
  created_at: string;
  updated_at: string;
}
