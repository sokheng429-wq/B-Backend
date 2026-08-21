---
name: Backend runtime gotchas (dev server, auth behavior)
description: Non-obvious runtime facts verified 2026-08-21 — port 8081 occupied by a user's running instance; unauthenticated protected endpoints return 302 not 401; smoke-test pattern on 8082.
type: project
---

- Port **8081 is occupied by a long-running user instance** (verified 2026-08-21, still listening afterward — do NOT kill it). For local dev/smoke runs use
  `mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--server.port=8082`.
- **Unauthenticated calls to protected endpoints return HTTP 302** (Spring Security form-login redirect), not 401 — pre-existing SecurityConfig behavior, project-wide (verified identical on `/api/users/me`). Frontend still receives 200/201/400/404/409 once the Bearer token is attached.
- **Why:** the default security entry point redirects instead of sending a JSON 401; the app relies on the frontend attaching the token, so 401 is never exercised in practice.
- **How to apply:** when smoke-testing protected routes, expect 302 only for the no-token probe; do not "fix" it, and never add permitAll for /api/members/**.
- Smoke test flow that works (dev/H2): register → login with `identifier=+855XXXXXXXXX` → token → CRUD on the endpoint with `Authorization: Bearer`.
