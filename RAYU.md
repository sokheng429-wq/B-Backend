# RAYU.md

This file provides guidance to RAYU when working with code in this repository.

## What this is

Spring Boot 3.3.4 (Java 17) auth backend for the B'Groceries app (Cambodia). Covers auth:
register, identifier login (username / full name / email / telegram / facebook / phone),
OTP-based login and forgot-password, and social login three ways: (1) `POST /api/auth/social`
with a cryptographically verified provider token (Gmail / Telegram / Facebook Login Widget),
(2) Spring Security's server-side OAuth2 login for Google / Facebook
(`/oauth2/authorization/{provider}` → redirect with `?token=`), and (3) a Telegram bot
webhook login flow (`t.me/BGroceriesBot?start=<token>`). Serves the `B-Frontend` React app,
so request field names (`username`, `fullName`, `email`, `phoneNumber`, `password`,
`confirmPassword`, `otp`, `identifier`, `provider`) are part of the API contract — do not
rename. Phone number + email are REQUIRED at manual register (phone for contact); social
accounts are created without a phone (`phone_number` is nullable).

Full API reference and run instructions are in `README.md`, but note it is partially stale
(it says dev profile / port 8080; actually **prod profile / port 8081** is the default). The
Telegram flow is documented in `TELEGRAM_LOGIN_SETUP.md`.

## Commands

Requires Maven. The `prod` profile (Neon PostgreSQL) is active by default — that is the
real database the frontend registers against. Use `dev` for a zero-setup H2 instance.

```bash
mvn spring-boot:run                                      # run with Neon (default, prod profile) on :8081
mvn spring-boot:run -Dspring-boot.run.profiles=dev       # run with H2 in-memory instead
mvn test                                                 # run all tests (pinned to dev/H2)
mvn test -Dtest=AuthServiceSocialTest                    # run a single test class
mvn test -Dtest=GoogleSocialVerifierTest                 # social verifier unit tests
mvn clean test                                           # clean first (recommended, see gotcha #3)
mvn package                                              # build jar
```

Neon connection is configured in `application-prod.yml` (env-overridable `DB_URL`,
`DB_USERNAME`, `DB_PASSWORD`, plus `JWT_SECRET` from `application.yml`). OTP codes are
NOT returned in API responses under `prod` — read them from the backend console log
(`=== [SMS SIMULATION] OTP ... ===`) while no real SMS gateway is wired up.

H2 console for inspecting data: `http://localhost:8081/h2-console`, JDBC URL
`jdbc:h2:mem:bgroceries`, user `sa`, empty password.

Telegram bot webhook helpers live at the repo root: `setup-telegram-webhook.sh/.bat`
(registers the webhook URL with the Telegram Bot API; expects the `shaggy-socks-shout`
localtunnel subdomain to be running) and `test-telegram-endpoints.sh/.bat`.

## Environment gotchas (this machine)

1. **JDK/Lombok (FIXED in `pom.xml`).** `JAVA_HOME` points at JDK 26 while the project
   targets Java 17. The Lombok version managed by Spring Boot 3.3.4 (1.18.34) and the
   default javac annotation-processing behavior (disabled on JDK 23+ unless requested)
   caused Lombok to generate nothing — `cannot find symbol` for every Lombok member.
   Fixed by overriding `<lombok.version>1.18.40</lombok.version>` and
   `<maven.compiler.proc>full</maven.compiler.proc>` in `pom.xml`. If you bump Spring Boot,
   re-check that its default Lombok supports the installed JDK.

2. **Corrupted `spring-security-web` jar (WORKED AROUND, PARTIAL).** The spring-security-web
   jar on this machine is missing the `org.springframework.security.web.filter` AND
   `org.springframework.security.web.cors` packages (verified in 6.3.1) — a network-level
   poisoned download. `JwtAuthFilter` therefore implements `jakarta.servlet.Filter` directly
   instead of `OncePerRequestFilter`. Consequence: **do not import from
   `org.springframework.security.web.filter.*` or `org.springframework.security.web.cors.*`.**
   However the `org.springframework.security.web.authentication` package IS intact, which is
   why `spring-boot-starter-oauth2-client` (now in `pom.xml`) and the server-side OAuth2
   login work: `SecurityConfig` uses `UsernamePasswordAuthenticationFilter` and the OAuth2
   handlers extend `SimpleUrlAuthenticationSuccessHandler/FailureHandler`. Only `OncePerRequestFilter`
   and CORS classes are unavailable.

3. **Stale `target/` poisons builds.** IntelliJ's failed compiles leave `.class` files that
   throw `java.lang.Error: Unresolved compilation problems` at runtime (e.g. on Spring
   context load). Always `mvn clean` before `mvn test`/`mvn spring-boot:run` when `target/`
   contains IDE-built classes.

## Architecture

Layered Spring Boot app, all under `com.bgroceries.backend`. Six controllers:

- `controller/AuthController` — 8 `POST` endpoints under `/api/auth`: `register`, `login`,
  `social`, `login/otp/send`, `login/otp/verify`, `forgot-password/send-otp`,
  `forgot-password/verify-otp`, `forgot-password/reset`. Returns `ApiResponse<T>`
  `{success, message, data}` envelopes.
- `controller/UserController` — `GET/PUT /api/users/me` (authenticated). `GET` returns the
  profile; `PUT` applies a partial `UpdateProfileRequest` (fullName, email, phoneNumber,
  dateOfBirth, gender, nationality) via `UserService.updateProfile`, which returns a **fresh
  `AuthResponse`** so the frontend keeps a valid token even when the phone (the JWT subject)
  changes. Lookup is by JWT subject: phone first, then username.
- `controller/TelegramAuthController` — `POST /api/auth/telegram/init` (creates a
  `LoginSession`, returns its token) and `GET /api/auth/telegram/status/{token}` (frontend
  polls until `COMPLETED`, then receives the JWT + user).
- `controller/TelegramWebhookController` — `POST /api/telegram/webhook` (public). Telegram
  bot updates; handles `/start <sessionToken>` by delegating to `TelegramService`.
- `controller/OAuth2TestController` / `controller/OAuth2DiagnosticController` — dev-only
  helpers (`/api/oauth2/config`, `/api/oauth2/diagnostic/**`) that echo configured
  credentials/connectivity. **Marked "REMOVE IN PRODUCTION" in their Javadoc.**
- Static `src/main/resources/static/oauth-test.html` — dev-only OAuth2 test page.

### Social login — three parallel implementations

- **Token-verified `/api/auth/social`** (`service/AuthService.socialLogin`): REAL OAuth when
  the request carries a `token` — it picks the matching `SocialVerifier` (see `social/`),
  which cryptographically verifies the provider credential, then find-or-creates the account
  by provider id (`google_id`/`facebook_id`/`telegram_id`) or provider-verified email.
  Without a token it keeps the SIMULATED demo behavior: one-click demo accounts per provider
  (`gmail.demo@bgroceries.demo` / `telegram.demo` / `facebook.demo`) when `identifier` is
  omitted, else legacy identifier lookup. Social users get a random BCrypt password and no
  phone; their JWT subject is the username, and `CustomUserDetailsService` falls back to
  username lookup.
- **Server-side OAuth2** (`security/oauth/`): Spring Security `oauth2Login` for Google +
  Facebook. User hits `/oauth2/authorization/{provider}`, is redirected back to
  `/login/oauth2/code/{registrationId}`, `CustomOAuth2UserService` (extends
  `DefaultOAuth2UserService`) find-or-creates the local `User` by providerId (with
  email-linking), wraps it in `CustomOAuth2User`, and `OAuth2LoginSuccessHandler` mints a JWT
  and 302-redirects to `app.oauth2.redirect-uri` (default `http://localhost:5173/oauth2/redirect`)
  as `?token=<jwt>` (or `?error=` on failure). Client registrations live under
  `spring.security.oauth2.client.*` in `application.yml`; the success handler uses `username`
  as the JWT subject regardless of phone. Sessions are `IF_REQUIRED` (not stateless) because
  Spring Security's OAuth2 dance needs them; JWT-authenticated API calls still use no session.
- **Telegram bot flow** (`service/TelegramService` + `LoginSession`): frontend calls
  `/api/auth/telegram/init` → gets a random token → opens `https://t.me/BGroceriesBot?start=<token>`.
  Telegram delivers `/start <token>` to the webhook; `TelegramService.processStartCommand`
  validates the session (PENDING, not expired — 5 min), find-or-creates the `User` by
  `telegramUserId` (generating a unique username like `<handle>_<tgUserId>`, random password,
  `loginProvider=telegram`, no phone), mints a JWT, stores it on the session and marks it
  `COMPLETED`. The frontend polls `status/{token}` and picks up `{status, token, user}`.
  Requires the webhook registered with the Bot API (see `setup-telegram-webhook.sh`) —
  Telegram bot token is `telegram.bot.token`.

### Core services

- `service/AuthService` — orchestrates register/login/OTP/social flows: normalizes phone,
  validates password match, resolves the login identifier across username → full name →
  email → telegram → facebook → phone, delegates OTP work, builds `AuthResponse` with a JWT.
  `@Transactional` on write flows.
- `service/OtpService` — generates a numeric code, persists a BCrypt **hash** (`OtpCode`
  row keyed by phone + `OtpPurpose` LOGIN/RESET_PASSWORD), sends via `SmsService`, and
  verifies (expiry, max attempts, BCrypt match, marks used). Returns the raw code only when
  `app.otp.expose-in-response=true` (dev).
- `service/SmsService` → `service/impl/ConsoleSmsServiceImpl` — SMS is behind an interface;
  the only impl logs the code (dev). Wire a real Cambodian gateway impl before prod; only
  one `SmsService` bean may exist.
- `service/UserService` — profile update (`updateProfile`), validates uniqueness of the new
  email/phone against other users, re-issues a JWT in case the subject changed.
- `social/` — real social-login verification. `SocialVerifier` interface + one `@Component`
  per provider: `GoogleSocialVerifier` (provider `"gmail"`; verifies Google ID tokens
  against `https://www.googleapis.com/oauth2/v3/certs` via jjwt `Jwks`/`Locator`, 1h JWKS
  cache, requires `aud` = client id + `email_verified`), `FacebookSocialVerifier`
  (Graph API `debug_token` with app token `appId|appSecret` + profile fetch), and
  `TelegramSocialVerifier` (Login Widget `auth` JSON; official HMAC-SHA256 with key
  `SHA256(botToken)`; rejects `auth_date` older than 24h). All fail with an opaque 401
  "Invalid provider token". Credentials come from `app.social.*` (env vars); placeholder
  values fail safe before any network call. `AuthService` injects the beans as
  `List<SocialVerifier>`. RestClient-based. **This is separate from the
  `spring-boot-starter-oauth2-client` server-side OAuth2 flow** — both coexist.
- `security/` — `JwtUtil` (jjwt 0.12; ACCESS tokens 24 h with `type=ACCESS`, `userId`,
  `role`, subject = **phone if present else username**; RESET tokens 10 min `type=RESET`),
  `JwtAuthFilter` (implements `jakarta.servlet.Filter`, see gotcha #2; requires
  `type=ACCESS` Bearer token, loads user by subject via `CustomUserDetailsService` which
  tries phone first then username), `oauth/` handlers described above.
- `config/SecurityConfig` — CSRF off, CORS via the `CorsConfig` bean, sessions
  `IF_REQUIRED`, `oauth2Login` wired, permits `/api/auth/**`, `/api/telegram/webhook`,
  `/api/oauth2/**`, `/oauth2/**`, `/login/oauth2/code/**`, `/oauth-test.html`,
  `/h2-console/**` and OPTIONS; `/api/admin/**` requires `ROLE_ADMIN`; everything else
  authenticated. `DataInitializer` seeds `admin`/`admin123` (ADMIN) — change the password!
  `PasswordEncoderConfig` supplies the BCrypt `PasswordEncoder` bean.
- `exception/` — `ApiException` carries an `HttpStatus`; subclasses `BadRequestException`
  (400), `NotFoundException` (404), `ConflictException` (409), `UnauthorizedException` (401).
  `GlobalExceptionHandler` (`@RestControllerAdvice`) maps these, plus Bean Validation field
  errors (returned in `data` as `{field: message}`), `BadCredentialsException`, and a
  generic 500.
- `entity/` — `User` (login identifiers: `username`, `fullName`, `email`, `telegram`,
  `facebook`, all unique where sensible; `phoneNumber` unique, nullable (social accounts);
  `passwordHash`, `enabled`, auto timestamps; nullable unique provider ids `googleId`/
  `facebookId`/`telegramId`, plus numeric `telegramUserId` used by the bot flow;
  `loginProvider` (`"google"`/`"facebook"`/`"telegram"`/null) tracks which OAuth flow logged
  in; optional profile fields `dateOfBirth`, `gender`, `nationality`) and `OtpCode`, and
  `LoginSession` (`login_sessions`: token, telegram ids, `status` PENDING/COMPLETED/EXPIRED,
  `jwtToken`, `expiresAt`). `repository/` — Spring Data JPA interfaces; note the OTP lookup
  `findTopByPhoneNumberAndPurposeAndUsedFalseOrderByCreatedAtDesc`, the identifier lookups
  (`findByUsernameIgnoreCase`, `findByEmailIgnoreCase`, `findByFullNameIgnoreCase`,
  `findByTelegram`, `findByFacebook`, `findByPhoneNumber`), the provider-id lookups
  (`findByGoogleId`, `findByFacebookId`, `findByTelegramId`), and `findByTelegramUserId`.
- `config/` — `CorsConfig` (the `CorsConfigurationSource` bean — wide open for the Vite dev
  server on :5173), `DataInitializer`, `PasswordEncoderConfig`, `SecurityConfig`.
- `util/PhoneUtil` — canonicalizes all input formats (`012...`, `855...`, `+855...`) to
  `+855XXXXXXXXX` before any DB lookup/insert. Always normalize phone numbers in new code.
- Config: `application.yml` (base: prod profile active by default, `app.jwt.*`, `app.otp.*`,
  `app.social.*`, `spring.security.oauth2.client.*`, `telegram.bot.*`, `telegram.webhook.*`,
  `app.oauth2.redirect-uri`, `spring.http.client.*` timeouts), `application-dev.yml` (H2,
  `ddl-auto: update`, `show-sql: true`, `otp.expose-in-response: true`), `application-prod.yml`
  (PostgreSQL from env vars, `expose-in-response: false`).

### Flow notes

- Register/login/OTP-login all return `{token, user}` (auto-login on register; `AuthResponse`
  has no `tokenType` field anymore).
- Forgot password is three steps: send OTP → verify OTP (returns a short-lived `resetToken`)
  → reset with the token. `resetPassword` re-checks `type=RESET` and expiry.
- OTP codes: 6 digits, 5 min expiry, max 5 wrong attempts (configurable), BCrypt-hashed at
  rest. In dev the code is echoed back as `debugOtp` and printed to console.
- JWT subject is the **phone if the user has one, otherwise the username** (social-only
  accounts). Any code that resolves "the current user" from the subject must try phone first,
  then username — see `CustomUserDetailsService`, `UserController`, `TelegramService`.
- New resource endpoints (products, orders…) will be protected by default and can reuse
  `JwtUtil`/`JwtAuthFilter` as-is.

## Tests

- `mvn test` is pinned to the `dev`/H2 profile.
- `AuthServiceSocialTest` (`@DataJpaTest`) exercises the token-verified social login against
  a hand-rolled `FakeVerifier` — **no Mockito anywhere in this repo**: class mocking is
  broken on the JDK 26 host, so tests use real collaborators + reflection to set JWT fields.
- `social/` unit tests: `GoogleSocialVerifierTest`, `FacebookSocialVerifierTest`,
  `TelegramSocialVerifierTest`.

## Conventions

- Lombok is used pervasively (`@RequiredArgsConstructor`, `@Builder`, `@Getter/@Setter`,
  `@Slf4j`) — this is why the JDK/Lombok mismatch (#1) breaks everything.
- DTOs: `dto/request` (with Bean Validation annotations) and `dto/response`; build responses
  with `ApiResponse.success(message, data)`.
- Throw `ApiException` subclasses from services; never return raw 500s (the handler catches
  the rest).
- Profile-sensitive behavior (H2 vs Postgres, `debugOtp`) is driven by `application-*.yml`;
  put new env-var-driven settings under `app.*` following the existing pattern.

## Security note — credentials checked into source control

`application.yml` currently contains **real OAuth credentials hardcoded as defaults**:
the Google client-id + client-secret, Facebook app-id + app-secret, and the Telegram bot
token; `application-prod.yml` has a real Neon PostgreSQL `DB_PASSWORD` default. Also
`suspect: `telegram.webhook.secret` defaults to `https://shaggy-socks-shout.loca.lt` — that
is a localtunnel URL, not a secret token (looks like a copy-paste error; the webhook secret
should be a random string, and the URL belongs in the tunnel config / setup script).
Flag all of this to the user: these should be env-only (no hardcoded fallbacks) and every
value that has been pushed to git should be rotated.
