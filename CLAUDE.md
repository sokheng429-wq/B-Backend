# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

B'Groceries authentication backend (Spring Boot 3.3.4, Java 17) for the Cambodia-based B'Groceries app. Covers auth (register, identifier login, OTP login/forgot-password), social login three ways, plus Jobs/Job Applications and Members management modules with admin endpoints. Serves the `B-Frontend` React app — request field names (`username`, `fullName`, `email`, `phoneNumber`, `password`, `confirmPassword`, `otp`, `identifier`, `provider`) are part of the API contract; do not rename them.

`RAYU.md` is an older but still largely accurate architecture guide with machine-specific gotchas worth reading. `README.md` is partially stale (says dev profile / port 8080).

## Development Commands

```bash
mvn spring-boot:run                                      # run with default profile (prod = Neon PostgreSQL) on :8081
mvn spring-boot:run -Dspring-boot.run.profiles=dev       # run with H2 in-memory instead (zero setup)
mvn test                                                 # all tests (pinned to dev/H2)
mvn test -Dtest=AuthServiceSocialTest                    # single test class
mvn clean test                                           # clean first — see gotcha #3
mvn clean package                                        # build jar
```

**Always `mvn clean` before `test`/`spring-boot:run` when `target/` contains IDE-built classes** — IntelliJ's failed compiles leave `.class` files that throw `Unresolved compilation problems` at runtime.

Profiles:
- **prod (default)** — Neon PostgreSQL via env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) with hardcoded fallbacks in `application-prod.yml`; `app.otp.expose-in-response: false`.
- **dev** — H2 at `jdbc:h2:mem:bgroceries` (`/h2-console`, user `sa`, empty password), `show-sql`, OTP codes echoed back as `debugOtp`.

With no SMS gateway wired up, OTP codes are printed to console: `=== [SMS SIMULATION] OTP ... ===` (read them here under prod).

## Environment gotchas (this machine)

1. **JDK 26 host / Java 17 target**: `pom.xml` pins `<lombok.version>1.18.40</lombok.version>` and `<maven.compiler.proc>full</maven.compiler.proc>` because javac 23+ disables annotation processing by default. If you bump Spring Boot, re-check its managed Lombok version supports the installed JDK.
2. **Corrupted local `spring-security-web` jar**: the `org.springframework.security.web.filter` and `.cors` packages are unusable. **Do not import from them.** `JwtAuthFilter` implements `jakarta.servlet.Filter` directly (not `OncePerRequestFilter`). The `.authentication` package IS intact — that's why server-side OAuth2 works.
3. **No Mockito**: class mocking is broken on this JDK. Tests use real collaborators + hand-rolled fakes (see `AuthServiceSocialTest.FakeVerifier`) and reflection to set fields.

## Architecture

Layered Spring Boot app under `com.bgroceries.backend`: `controller/` → `service/` → `repository/` (JPA) → `entity/`. All responses use the `ApiResponse<T>` envelope `{success, message, data}`. Errors: throw `ApiException` subclasses (`BadRequestException` 400, `UnauthorizedException` 401, `NotFoundException` 404, `ConflictException` 409); `GlobalExceptionHandler` maps them plus Bean Validation errors (returned in `data` as `{field: message}`).

### Endpoints

- `/api/auth/**` (public): `register`, `login`, `social`, `telegram/init`, `telegram/status/{token}`, `login/otp/send|verify`, `forgot-password/send-otp|verify-otp|reset`
- `/api/users/me` (authenticated): GET/PUT profile; PUT returns a fresh `AuthResponse` because changing the phone changes the JWT subject
- `/api/public/jobs`, `/api/public/jobs/{id}/apply`, `/api/public/members` (public, safe fields only)
- `/api/admin/jobs|applications|users` (require `ROLE_ADMIN` via the `/api/admin/**` path matcher — no `@PreAuthorize` needed on those controllers)
- `/api/members` (authenticated CRUD)
- Dev-only: `/api/oauth2/**` test/diagnostic endpoints and `static/oauth-test.html` ("REMOVE IN PRODUCTION")
- `/api/telegram/webhook` (public) — Telegram bot updates

### Authentication flows

All methods converge into a JWT session (`JwtUtil`, jjwt 0.12). ACCESS tokens carry `type=ACCESS`, `userId`, `role`, and **subject = phone if present, else username** — any code resolving "the current user" must try phone first, then username (see `CustomUserDetailsService`, `UserController`, `TelegramService`). RESET tokens have `type=RESET` and 10-min expiry.

1. **Register/password/OTP login** (`AuthService`): phone normalized to `+855XXXXXXXXX` via `PhoneUtil` (always normalize before DB lookups). Login identifier resolves across username → full name → email → telegram → facebook → phone. Logging into a passwordless social account with a password sets that password.
2. **OTP**: `OtpService` stores only BCrypt hashes (`otp_codes`, keyed by phone + purpose LOGIN/RESET_PASSWORD); expiry 2 min, max 2 attempts (configurable). Forgot-password is three steps: send OTP → verify (returns short-lived `resetToken` JWT) → reset with token.
3. **Inactivity auto-logout**: `TokenActivityStore` (in-memory) tracks last-seen per token; `JwtAuthFilter` rejects tokens idle > 5 min with HTTP 401 `{"error":"SESSION_TIMEOUT"}` and evicts them. Tokens issued before this feature exist are also rejected (never registered).

### Social login — three parallel implementations (all coexist)

- **Token-verified `POST /api/auth/social`**: when the request carries a `token`, the matching `SocialVerifier` (`social/` package) cryptographically verifies it server-side — `GoogleSocialVerifier` (ID token vs Google JWKS, checks `aud` + `email_verified`, 1h JWKS cache), `FacebookSocialVerifier` (Graph `debug_token`), `TelegramSocialVerifier` (Login Widget HMAC-SHA256, key = `SHA256(bot_token)`). Find-or-create by provider ID or provider-verified email. Without a `token`, falls back to simulated demo accounts (`gmail.demo@bgroceries.demo` etc.). Verifiers fail safe with opaque 401 when credentials are placeholders. `AuthService` injects them as `List<SocialVerifier>`.
- **Server-side OAuth2** (`security/oauth/`): Spring Security `oauth2Login` for Google/Facebook. `/oauth2/authorization/{provider}` → callback → `CustomOAuth2UserService` find-or-creates the user → `OAuth2LoginSuccessHandler` mints a JWT and 302s to `app.oauth2.redirect-uri` (default `http://localhost:5173/oauth2/redirect`) as `?token=<jwt>`. Client registrations live under `spring.security.oauth2.client.*`. Sessions are `IF_REQUIRED` (not stateless) because the OAuth2 dance needs them; plain JWT API calls still use no session.
- **Telegram bot flow** (`service/TelegramService` + `LoginSession` entity): frontend calls `/api/auth/telegram/init` → opens `t.me/BGroceriesBot?start=<token>` → webhook delivers `/start <token>` → session marked COMPLETED with stored JWT → frontend polls `status/{token}`. Requires webhook registration (`setup-telegram-webhook.sh/.bat`, expects a localtunnel URL).

Social-created users get a random BCrypt password, no phone, unique generated username; their JWT subject is the username.

### Data model highlights

`User` carries multiple optional identity fields: `phoneNumber` (unique, nullable — social accounts have none), `username`, `fullName`, `email`, `telegram`, `facebook`, provider IDs `googleId`/`facebookId`/`telegramId` (+ numeric `telegramUserId` for the bot flow), `loginProvider`, role (`USER`/`ADMIN`), optional profile fields. `DataInitializer` seeds `admin`/`admin123`. Other entities: `OtpCode`, `PasswordResetOtp`, `LoginSession`, `Member`(+`MemberDetail`), `Job`, `JobApplication`.

### Configuration

- `application.yml` — base config: prod profile active, `app.jwt.*` (24h access, 10min reset, 5min inactivity timeout), `app.otp.*`, `app.social.*`, `spring.security.oauth2.client.*`, `telegram.bot.*`, Gmail SMTP for forgot-password email (`GMAIL_SMTP_USERNAME`/`GMAIL_SMTP_PASSWORD` env vars; empty → console log via `SmtpEmailServiceImpl`)
- `application-dev.yml` / `application-prod.yml` — datasource + OTP exposure per profile
- Env-overridable throughout following the `${ENV_VAR:default}` pattern; put new settings under `app.*`
- CORS: `CorsConfig` bean (wide open for Vite dev server :5173)

### Conventions

- Lombok pervasively (`@RequiredArgsConstructor`, `@Builder`, `@Slf4j`) — see gotcha #1
- DTOs split `dto/request` (Bean Validation annotations) and `dto/response`
- SMS behind `SmsService` interface (only impl: console logger; exactly one bean may exist). Email behind `EmailService` (SMTP impl with console fallback).
- Profile-sensitive behavior driven by `application-*.yml`, not code branches
