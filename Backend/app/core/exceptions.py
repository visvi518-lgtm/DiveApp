class AppException(Exception):
    """Base exception for all application-level errors.

    Carries an HTTP status code and a stable error code so the API
    returns a consistent error response shape across every endpoint.
    """

    def __init__(self, status_code: int, code: str, message: str):
        self.status_code = status_code
        self.code = code
        self.message = message
        super().__init__(message)


class NotFoundException(AppException):
    def __init__(self, message: str = "Resource not found"):
        super().__init__(status_code=404, code="NOT_FOUND", message=message)


class ConflictException(AppException):
    def __init__(self, message: str = "Resource already exists"):
        super().__init__(status_code=409, code="CONFLICT", message=message)


class ValidationException(AppException):
    def __init__(self, message: str = "Invalid request"):
        super().__init__(status_code=422, code="VALIDATION_ERROR", message=message)


class UnauthorizedException(AppException):
    def __init__(self, message: str = "Authentication required"):
        super().__init__(status_code=401, code="UNAUTHORIZED", message=message)


class ForbiddenException(AppException):
    def __init__(self, message: str = "Access denied"):
        super().__init__(status_code=403, code="FORBIDDEN", message=message)
