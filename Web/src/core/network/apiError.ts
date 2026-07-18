/** Mirrors the backend's standardized error body: {"error": {"code", "message"}}. */
export interface ServerErrorBody {
  error: {
    code: string;
    message: string;
  };
}

export type ApiErrorKind = 'network' | 'server' | 'unauthorized' | 'unknown';

export class ApiError extends Error {
  readonly kind: ApiErrorKind;
  readonly statusCode?: number;
  readonly code?: string;

  private constructor(kind: ApiErrorKind, message: string, statusCode?: number, code?: string) {
    super(message);
    this.kind = kind;
    this.statusCode = statusCode;
    this.code = code;
  }

  static network(): ApiError {
    return new ApiError('network', '네트워크 연결을 확인해주세요.');
  }

  static unauthorized(): ApiError {
    return new ApiError('unauthorized', '로그인이 만료되었습니다. 다시 로그인해주세요.');
  }

  static server(statusCode: number, code: string, message: string): ApiError {
    return new ApiError('server', message, statusCode, code);
  }

  static unknown(): ApiError {
    return new ApiError('unknown', '요청을 처리하지 못했습니다.');
  }
}
