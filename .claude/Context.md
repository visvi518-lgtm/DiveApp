# Project Context

## Project

DiveApp

---

## Current Phase

Android (Reports 04-05) and Web (Report 06) both complete and build/browser-verified. Note: "Phase" numbering got ambiguous — Report 04's title says "Phase 4" for Android foundation, but the original plan's Phase 4 meant iOS feature screens. Prefer referring to Report file numbers (01-06...) over "Phase N" going forward.

---

## Platform Priority Decision (2026-07-14)

User decided: **iOS development is PAUSED until the user explicitly asks to resume it.** Priority is now Android (already build-verified in this env) and a Web consumer app (React + Vite, same features as mobile — not an admin dashboard). Reason: this environment has no Mac/Xcode so iOS can't be verified at all, while Android and Web both can be. Do not start iOS work unless the user brings it up again. See memory `project_diveapp.md` for the persistent record of this decision.

---

## Current Status

iOS SwiftUI foundation (Report 03) still UNVERIFIED — no Mac/Xcode in this environment, and now explicitly PAUSED (see decision above). Android (Reports 04-05) and Web (Report 06) are both feature-complete for MVP domains and were UI-smoke-tested. Report 07 went further: stood up a real local PostgreSQL (portable EDB zip, port 5433) + FastAPI backend in this environment and ran genuine end-to-end tests — found and fixed 3 real bugs (Alembic enum double-create, 5 models missing `DateTime(timezone=True)` causing a live 500 on `/banners`, and Android blocking cleartext HTTP by default with no network security config). Confirmed real data (seeded via direct API calls) renders correctly in both the Android app (logcat showed real 200 OK responses before an emulator crash cut the screenshot short) and the Web app (screenshot showing the real seeded nickname and dive log). Wrote `DiveApp/START.md` (mirroring `health-web/START.md`) as a plain local-setup guide for the user to run backend+frontends themselves.

Report 08: replaced the Naver/Google login stubs with real SDK integrations on Android (`com.navercorp.nid:oauth` NidOAuth + Credential Manager/Google Identity Services library) and Web (Naver Login JS SDK + Google Identity Services JS, both via a "render the real button off-screen and forward the click" pattern since neither SDK supports a fully custom trigger). Backend needed no changes — it already expected a raw provider token (Naver access token / Google ID token) from the client. Build-verified on both platforms; ran on the real Android emulator and confirmed both buttons fail fast with a friendly Korean message (no crash, no hang) when Client IDs are empty — real account login is unverified since this environment has no registered Naver/Google OAuth apps. The user must fill in real Client ID/Secret values themselves (`Web/.env`, `Android/local.properties`, `Backend/.env`) per `START.md` section 3단계-C.

Report 09: built the Admin dashboard as `/admin/*` routes inside the existing Web app (not a separate app) — dashboard stats, user search/detail/suspend-unsuspend, information article CRUD, banner CRUD, all gated by a `RequireAdmin` guard mirroring the existing `RequireAuth` style, with the nav link only shown to `currentUser.role === 'ADMIN'`. Verified against the still-running local backend from Reports 07-08 by promoting the test account to ADMIN and driving headless Chrome through every screen — confirmed real stats render, a real backend uniqueness-constraint error (banner `display_order`) surfaces correctly in the form instead of crashing, and create/delete actually persist (verified via before/after list counts). Two backend API gaps worked around on the client: no admin single-article/banner GET (edit pages use router state, falling back to a full list refetch), and admin user responses have no `role` field (so the UI can't show/change who else is an admin).

Report 10: added email/password login+register to Web only (user's explicit scope). Backend got a real schema change — `AuthProvider.EMAIL` added to the enum (migration 0002, using `autocommit_block()` for the required `ALTER TYPE ... ADD VALUE`) and a nullable `User.password_hash` column (bcrypt-hashed) — plus `POST /auth/register` and `POST /auth/login/email`, both registered before the existing dynamic `/login/{provider}` route to avoid a path-matching collision. Reused the exact same `TokenResponse`/`is_new_user` contract as social login, so email signup flows into the same ProfileSetup screen. While testing the full register→setup→home→logout→re-login cycle for the first time ever in a real browser (previous reports only exercised existing accounts or auth-bypassed UI), found and fixed two pre-existing routing bugs that also affected social login's new-user path: `/login` (`PublicOnly`) never redirected on `needsProfileSetup`, and `/profile-setup` had no guard at all so it never left after a successful save. Also hit an infrastructure snag mid-session — the portable Postgres's `share/timezonesets/Default` file had gone missing, so any fresh connection (but not the already-open uvicorn pool) failed; restored the exact file from the official postgres/postgres GitHub mirror (tag REL_16_4) and confirmed no data loss.

**Do not touch**: `DiveApp/DiveAppSwiftPlayground/` is the user's own iPad Swift Playground sync artifact, unrelated to this work — leave untouched until they explicitly say to delete it. (Correction: lowercase `DiveApp/report/` was previously also flagged here, but that was a mistake — this filesystem is case-insensitive, so `report/` and `Report/` are the same physical folder, i.e. this project's own report folder. No special caution needed there.)

---

## Completed

- Project initialized
- Git initialized
- Folder structure created
- README.md created
- CLAUDE.md established
- Workflow.md completed
- CodingRules.md completed
- PromptGuide.md completed
- Project documentation initialized
- Phase 1: FastAPI project structure (core/database/models/schemas/repositories/services/routers/middlewares)
- Phase 1: SQLAlchemy models for all 14 MVP tables per 11_DatabaseSchema.md
- Phase 1: Alembic initial migration (0001_initial_schema)
- Phase 1: JWT + Naver/Google OAuth login, refresh, profile setup, current user endpoints
- Phase 1: Report written at Report/01_report_backend_foundation.md
- Phase 2: Dive Log, CO2 Table(Training), Certification, Community, Information, Banner, Admin APIs (31 endpoints)
- Phase 2: Report written at Report/02_report_domain_apis.md
- Phase 3: SwiftUI project scaffold (XcodeGen project.yml), networking layer, Keychain-backed auth session, Naver/Google login stubs, Auth+User models/services/repositories, design system components, Splash/Login/ProfileSetup/RootTabView screens
- Phase 3: Report written at Report/03_report_ios_foundation.md — UNVERIFIED, no Mac available
- Phase 4: Android/Kotlin/Jetpack Compose project scaffold (Gradle, mirrors iOS Phase 3 scope: networking via Retrofit/OkHttp, EncryptedSharedPreferences-backed auth session, Naver/Google login stubs, Auth+User models/services/repositories, Compose design system, Splash/Login/ProfileSetup/RootTabScaffold screens)
- Report 04: Built and verified for real — assembleDebug succeeded, APK installed on Pixel_9 emulator, app launched with no crashes, login screen rendered correctly, button tap correctly surfaced the stub SDK error message
- Report 04: Report written at Report/04_report_android_foundation.md
- Report 05: Dive Log, CO2 Table(Training), Certification, Community, Information feature screens implemented — each tab now owns its own nested NavHost (list/detail/create/edit); Information nested under Home, Certification nested under My Page
- Report 05: Build-verified (1 compile error found and fixed, 3 deprecation warnings cleaned up) and UI-smoke-tested on emulator via a temporary AuthSession bypass (forced AUTHENTICATED, screenshotted every tab + the CO2 Table timer actually counting down in real time, then reverted and re-verified the revert both in source and by re-launching to confirm the Login screen shows again)
- Report 05: Report written at Report/05_report_android_feature_screens.md
- Report 06: Web consumer app (React + Vite + TypeScript + react-router-dom + axios) — same feature set as Android (Home, Dive Log, CO2 Table, Certification, Community, Information), same REST API, localStorage-backed auth session mirroring AuthSession
- Report 06: Build-verified (`npm run build` clean both times — a handful of real TS errors were caught and fixed on the foundation build, zero errors on the full feature build) and browser-verified via headless Chrome screenshots of every page plus a puppeteer-core-driven click test confirming the CO2 Table timer counts down in real time; temporary auth bypass used for smoke testing (no live backend), reverted and re-verified
- Report 06: Report written at Report/06_report_web_app.md
- Report 07: Stood up a real local PostgreSQL + FastAPI backend in this environment; ran genuine end-to-end tests against ~all 31 endpoints plus Android and Web clients. Found and fixed 3 real bugs (Alembic enum double-create in the migration, 5 ORM models missing `DateTime(timezone=True)`, Android missing a debug network security config to allow cleartext HTTP to 10.0.2.2/localhost)
- Report 07: Report written at Report/07_report_live_backend_e2e.md; wrote DiveApp/START.md as a local run guide for the user (mirrors health-web/START.md)
- Report 08: Real Naver/Google login SDKs wired up on Android and Web (see Current Status above)
- Report 08: Report written at Report/08_report_social_login_sdk.md; START.md updated with a "3단계-C" section documenting how to register real OAuth apps and where to put the resulting Client ID/Secret values
- Report 09: Admin dashboard web screens (dashboard/users/information/banners) built inside the existing Web app, gated by a RequireAdmin route guard (see Current Status above)
- Report 09: Report written at Report/09_report_admin_dashboard.md
- Report 10: Email/password login+register added to Web (Backend schema change + 2 endpoints, Web LoginPage redesign, 2 pre-existing routing bugs fixed) — see Current Status above
- Report 10: Report written at Report/10_report_email_auth.md
- Report/howToStart.md and Report/toDoList.md written per user request: a quick local-run runbook (Backend/Web/Android) and a checklist of API keys per platform + a deployment checklist
- Web HomePage visual redesign (no report — small polish task, not a new feature): gradient hero card with greeting + real dive-log stats (total dives/max depth, hidden if none), restyled banner placeholder, and a 4-item feature quick-access grid (Dive Log/CO2 Table/Community/Information) replacing the old single plain text link. No new dependencies (still emoji + CSS, no icon library)
- Real Naver/Google OAuth apps registered by the user (2026-07-18). Note: despite what was recorded earlier, Android's `local.properties` was never actually filled in (only `sdk.dir`) — only Backend/.env and Web/.env got real values. User then completed a REAL login with their own Naver and Google accounts on Web end-to-end (first time this project has had a real social login actually succeed), which surfaced and led to fixing two real bugs:
  1. Naver: `NaverCallbackPage`'s SDK instance never had `.init()` called on it, so the SDK never parsed the OAuth response out of the URL — always failed with "로그인이 완료되지 않았습니다" regardless of whether the real Naver login succeeded. Confirmed root cause by re-fetching and reading the actual minified SDK source (found `init()` is what triggers `oauthCallback()` internally). Fixed in `core/auth/socialAuth.ts`'s `createNaverLoginForCallback()`.
  2. Google: `Backend/.env`'s `GOOGLE_CLIENT_ID` and `Web/.env`'s `VITE_GOOGLE_CLIENT_ID` had been filled in with two different Google OAuth client IDs (user's mistake), causing the backend's `aud` check to always fail with 401. Fixed by asking the user which was correct and syncing Backend/.env to match.
  Both now confirmed working with real accounts by the user directly.
- Report 11: Deployed Backend + PostgreSQL + Web to Render via a `render.yaml` Blueprint. Found and fixed 4 real deployment issues (DATABASE_URL driver scheme needing a Pydantic validator to handle Render's plain `postgresql://`, `preDeployCommand` not supported on the free plan so the migration got folded into `startCommand`, the account already having a free-tier Postgres elsewhere so DiveApp's DB had to go on the `basic-256mb` paid plan, and Naver rejecting login until the "서비스 URL" — not just the callback URL — was also registered for the production domain). Verified end-to-end on the real deployment: health check, CORS preflight, a real registration hitting the real production DB, and the user personally completing real Naver and Google logins on `https://diveapp-web.onrender.com`. Report written at Report/11_report_deployment.md.
- DiveApp now lives at its own GitHub repo (`github.com/visvi518-lgtm/DiveApp`, public), separate from the user's other `workspace` monorepo. The user's personal `DiveAppSwiftPlayground/` folder was excluded via .gitignore (files untouched on disk, just untracked) per their standing instruction.
- Running backlog of unimplemented/unverified items maintained at Report/00_todo_backlog.md (Report 03 Xcode build verification remains the top priority item for iOS, which is paused; live-backend e2e testing, social login SDK integration, Admin dashboard, and production deployment are now done for Web; Android has live-backend e2e + social login SDK done but is not deployed anywhere)

---

## Current Task

User is going through a numbered list in order: 1) live backend e2e testing (DONE, Report 07), 2) real Naver/Google SDK integration (DONE, Report 08), 3) Admin dashboard web screens (DONE, Report 09), 4) other. Item 4 ("other") is not yet defined. In between (ad-hoc additions, not part of the numbered list): email/password login+register on Web (DONE, Report 10), a portfolio case-study writeup (PORTFOLIO.md/.html), and full Render deployment of Backend+DB+Web (DONE, Report 11). Ask the user what they want next, or offer the remaining backlog items (see Report/00_todo_backlog.md) unless redirected.

---

## Next Task

Continue logging unresolved items in Report/00_todo_backlog.md and one report per platform/feature milestone.

---

## Tech Stack

### Frontend

- iOS: Swift, SwiftUI, Swift Playground (Primary per Docs, but unverified in this env — PAUSED, see Platform Priority Decision above)
- Android: Kotlin, Jetpack Compose (build/emulator-verified in this env)
- Web: React + TypeScript + Vite + react-router-dom + axios, consumer-facing (build/browser-verified in this env)

### Backend

- FastAPI

### Database

- PostgreSQL (Render)

---

## Architecture

- MVVM
- Repository Pattern
- Service Layer

---

## Development Philosophy

1. Maintainability First
2. Readability First
3. Consistency First
4. Scalability First
5. Simplicity First
6. Security by Default
7. Documentation First

---

## Notes

This project prioritizes architecture and documentation before implementation.

No production code should be written until the planning phase is complete.
