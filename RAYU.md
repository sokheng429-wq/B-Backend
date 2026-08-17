# RAYU.md

This file provides guidance to RAYU when working with code in this repository.

## What this is

Spring Boot 3.3.4 (Java 17) auth backend for the B'Groceries app (Cambodia). Covers only auth:
register, identifier login (username / full name / email / telegram / facebook), simulated
social login (Gmail / Telegram / Facebook), and OTP-based forgot-password. Serves the
`B-Frontend` React app, so request field names (`username`, `fullName`, `email`,
`phoneNumber`, `password`, `confirmPassword`, `otp`, `identifier`, `provider`) are part of
the API contract — do not rename. Phone number + email are REQUIRED at manual register
(phone for contact); social accounts are created without a phone (`phone_number` is nullable).

Full API reference and run instructions are in `README.md` (read it first).

## Commands

Requires Maven. The `prod` profile (Neon PostgreSQL) is active by default — that is the
real database the frontend registers against. Use `dev` for a zero-setup H2 instance.

```bash
mvn spring-boot:run                                      # run with Neon (default, prod profile) on :8081
mvn spring-boot:run -Dspring-boot.run.profiles=dev       # run with H2 in-memory instead
mvn test                                                 # run all tests (pinned to dev/H2)
mvn test -Dtest=BackendApplicationTests#contextLoads     # run a single test/method
mvn clean test                                           # clean first (recommended, see gotcha #2)
mvn package                                              # build jar
```

Neon connection is configured in `application-prod.yml` (env-overridable `DB_URL`,
`DB_USERNAME`, `DB_PASSWORD`, plus `JWT_SECRET` from `application.yml`). OTP codes are
NOT returned in API responses under `prod` — read them from the backend console log
(`=== [SMS SIMULATION] OTP ... ===`) while no real SMS gateway is wired up.

H2 console for inspecting data: `http://localhost:8080/h2-console`, JDBC URL
`jdbc:h2:mem:bgroceries`, user `sa`, empty password.

## Environment gotchas (this machine)

The build was broken on this machine; both issues are now worked around in the repo:

1. **JDK/Lombok (FIXED in `pom.xml`).** `JAVA_HOME` points at JDK 26 while the project
   targets Java 17. The Lombok version managed by Spring Boot 3.3.4 (1.18.34) and the
   default javac annotation-processing behavior (disabled on JDK 23+ unless requested)
   caused Lombok to generate nothing — `cannot find symbol` for every Lombok member.
   Fixed by overriding `<lombok.version>1.18.40</lombok.version>` and
   `<maven.compiler.proc>full</maven.compiler.proc>` in `pom.xml`. If you bump Spring Boot,
   re-check that its default Lombok supports the installed JDK.

2. **Corrupted `spring-security-web` jars (WORKED AROUND in code).** Every
   spring-security-web jar on this network (all versions, from every mirror/CDN) is
   missing the `org.springframework.security.web.filter` package (which contains
   `OncePerRequestFilter`) — verified byte-identical corrupted downloads with matching
   checksums from repo.maven.apache.org, repo1.maven.org, Google's CDN, and Aliyun. This
   is network-level cache poisoning, not a Maven config issue. `JwtAuthFilter` therefore
   implements `jakarta.servlet.Filter` directly instead of `OncePerRequestFilter`.
   Consequence: **do not import anything from `org.springframework.security.web.filter.*`
   (or `org.springframework.security.web.cors.*`) — the classes are not on this machine's
   classpath.** If you need such a class, get a valid jar from outside this network first.

3. **Stale `target/` poisons builds.** IntelliJ's failed compiles leave `.class` files that
   throw `java.lang.Error: Unresolved compilation problems` at runtime (e.g. on Spring
   context load). Always `mvn clean` before `mvn test`/`mvn spring-boot:run` when `target/`
   contains IDE-built classes.

## Architecture

Layered Spring Boot app, all under `com.bgroceries.backend`. The whole API surface is a
single controller:

- `controller/AuthController` — 7 `POST` endpoints under `/api/auth`: `register`, `login`,
  `login/otp/send`, `login/otp/verify`, `forgot-password/send-otp`,
  `forgot-password/verify-otp`, `forgot-password/reset`. Returns `ApiResponse<T>`
  `{success, message, data}` envelopes.
- `service/AuthService` — orchestrates the flows: normalizes phone, validates password
  match, resolves the login identifier across username → full name → email → telegram →
  facebook (phone also still works), delegates OTP work, builds `AuthResponse` with a JWT.
  `socialLogin()` finds-or-creates the account per provider (gmail/telegram/facebook) —
  SIMULATED for now: the identifier is trusted as-is, no real OAuth handshake. Social users
  get a random BCrypt password and no phone; their JWT subject is the username, and
  `CustomUserDetailsService` falls back to username lookup. Wire real Google/Facebook OAuth +
  Telegram Login Widget before production. `@Transactional` on write flows.
- `service/OtpService` — generates a numeric code, persists a BCrypt **hash** (`OtpCode`
  row keyed by phone + `OtpPurpose` LOGIN/RESET_PASSWORD), sends via `SmsService`, and
  verifies (expiry, max attempts, BCrypt match, marks used). Returns the raw code only when
  `app.otp.expose-in-response=true` (dev).
- `service/SmsService` → `service/impl/ConsoleSmsServiceImpl` — SMS is behind an interface;
  the only impl logs the code (dev). Wire a real Cambodian gateway impl before prod; only
  one `SmsService` bean may exist.
- `security/` — `JwtUtil` (jjwt 0.12; generates ACCESS tokens — 24 h, claims `type=ACCESS`,
  `userId` — and short-lived RESET tokens — 10 min, `type=RESET`), `JwtAuthFilter`
  (implements `jakarta.servlet.Filter`; requires `type=ACCESS` Bearer token, loads user by
  phone via `CustomUserDetailsService`), `config/SecurityConfig` (stateless, CSRF off,
  permits `/api/auth/**` + `/h2-console/**`, everything else authenticated; CORS comes from
  the `CorsConfig` bean).
- `exception/` — `ApiException` carries an `HttpStatus`; subclasses `BadRequestException`
  (400), `NotFoundException` (404), `ConflictException` (409), `UnauthorizedException` (401).
  `GlobalExceptionHandler` (`@RestControllerAdvice`) maps these, plus Bean Validation field
  errors (returned in `data` as `{field: message}`), `BadCredentialsException`, and a
  generic 500.
- `entity/` — `User` (login identifiers: `username`, `fullName`, `email`, `telegram`,
  `facebook`, all unique where sensible; `phoneNumber` unique and required; `passwordHash`,
  `enabled`, auto timestamps) and `OtpCode`. `repository/` — Spring Data JPA interfaces;
  note the OTP lookup `findTopByPhoneNumberAndPurposeAndUsedFalseOrderByCreatedAtDesc` and
  the identifier lookups (`findByUsernameIgnoreCase`, `findByEmailIgnoreCase`, etc.).
- `config/` — `SecurityConfig` (stateless, CSRF off, `@EnableMethodSecurity`, permits
  `/api/auth/**` + `/h2-console/**`, requires `ROLE_ADMIN` for `/api/admin/**`, everything
  else authenticated), `CorsConfig` (the `CorsConfigurationSource` bean — wide open for the
  Vite dev server on :5173), and `DataInitializer` (seeds a default admin with role `ADMIN`:
  username `admin` / password `admin123` — change the password!). Users carry a `role`
  column (`USER`/`ADMIN`, null treated as `USER`) exposed in the JWT claim and
  `UserResponse`; manual + social accounts are `USER` — promote via
  `UPDATE users SET role='ADMIN' WHERE ...` in Neon.
- `util/PhoneUtil` — canonicalizes all input formats (`012...`, `855...`, `+855...`) to
  `+855XXXXXXXXX` before any DB lookup/insert. Always normalize phone numbers in new code.
- Config: `application.yml` (base: dev profile active, `app.jwt.*`, `app.otp.*` settings),
  `application-dev.yml` (H2, `ddl-auto: update`, `show-sql: true`,
  `otp.expose-in-response: true`), `application-prod.yml` (PostgreSQL from env vars,
  `expose-in-response: false`).

### Flow notes

- Register/login/OTP-login all return `{token, tokenType, user}` (auto-login on register).
- Forgot password is three steps: send OTP → verify OTP (returns a short-lived `resetToken`)
  → reset with the token. `resetPassword` re-checks `type=RESET` and expiry.
- OTP codes: 6 digits, 5 min expiry, max 5 wrong attempts (configurable), BCrypt-hashed at
  rest. In dev the code is echoed back as `debugOtp` and printed to console.
- `SecurityConfig` currently leaves everything outside `/api/auth/**` requiring a Bearer
  token — new resource endpoints (products, orders…) will be protected by default and can
  reuse `JwtUtil`/`JwtAuthFilter` as-is.

## Conventions

- Lombok is used pervasively (`@RequiredArgsConstructor`, `@Builder`, `@Getter/@Setter`,
  `@Slf4j`) — this is why the JDK/Lombok mismatch (#1) breaks everything.
- DTOs: `dto/request` (with Bean Validation annotations) and `dto/response`; build responses
  with `ApiResponse.success(message, data)`.
- Throw `ApiException` subclasses from services; never return raw 500s (the handler catches
  the rest).
- Profile-sensitive behavior (H2 vs Postgres, `debugOtp`) is driven by `application-*.yml`;
  put new env-var-driven settings under `app.*` following the existing pattern.

## Security note

`application-prod.yml` contains a hardcoded Neon PostgreSQL password as the `DB_PASSWORD`
default. Flag this to the user — it should be removed, env-only, and rotated.
