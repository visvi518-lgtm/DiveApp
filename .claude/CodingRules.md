# Coding Rules

## General Principles

- Follow SOLID principles.
- Follow Apple's Human Interface Guidelines (HIG).
- Prefer simplicity over complexity.
- Avoid duplicate code.
- Prioritize readability over cleverness.
- Write production-quality code.

---

## Swift

- Use SwiftUI.
- Follow MVVM architecture.
- Keep Views responsible only for UI.
- Move business logic to ViewModel or Service.
- Use Dependency Injection where appropriate.
- Avoid force unwrap (`!`).
- Prefer `guard` over nested `if`.
- Use meaningful naming.
- Use extensions to organize code.
- One file should have one primary responsibility.

---

## Backend

- Use FastAPI.
- Separate Router, Service, Repository, Model.
- Validate every request.
- Return consistent response models.
- Handle exceptions explicitly.

---

## Database

- PostgreSQL only.
- Normalize tables appropriately.
- Use UUID as primary key unless there is a justified reason not to.
- Never hardcode IDs.
- Design for future expansion.

---

## API

- Follow REST conventions.
- Use nouns for endpoints.
- Version APIs (`/api/v1`).
- Return proper HTTP status codes.
- Standardize error responses.

---

## Security

- Never hardcode API keys.
- Store secrets in environment variables.
- Validate all user input.
- Use authentication and authorization where required.

---

## Documentation

Whenever code changes:

- Update related documentation.
- Keep documentation synchronized with implementation.

---

## Git

- Small commits.
- One logical change per commit.
- Write meaningful commit messages.
- Never commit secrets or environment files.
