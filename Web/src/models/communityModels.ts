export interface AuthorResponse {
  id: string;
  nickname: string | null;
}

export interface CommunityPostCreateRequest {
  title: string;
  content: string;
}

export interface CommunityPostResponse {
  id: string;
  title: string;
  content: string;
  author: AuthorResponse;
  view_count: number;
  like_count: number;
  comment_count: number;
  is_pinned: boolean;
  created_at: string;
}

export interface CommunityPostListItem {
  id: string;
  title: string;
  author: AuthorResponse;
  view_count: number;
  comment_count: number;
  is_pinned: boolean;
  created_at: string;
}

export interface CommunityCommentCreateRequest {
  content: string;
}

export interface CommunityCommentResponse {
  id: string;
  content: string;
  author: AuthorResponse;
  created_at: string;
}
