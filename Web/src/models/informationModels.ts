export interface InformationArticleListItem {
  id: string;
  title: string;
  thumbnail_image_url: string | null;
  published_at: string | null;
}

export interface InformationArticleResponse {
  id: string;
  title: string;
  content: string;
  thumbnail_image_url: string | null;
  view_count: number;
  is_published: boolean;
  published_at: string | null;
  created_at: string;
  updated_at: string;
}

export interface InformationArticleCreateRequest {
  title: string;
  content: string;
  thumbnail_image_url?: string | null;
  is_published?: boolean;
}

export type InformationArticleUpdateRequest = Partial<InformationArticleCreateRequest>;
