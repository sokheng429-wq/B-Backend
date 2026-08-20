# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

B'Groceries authentication backend built with Spring Boot 3.3 and Java 17. Handles user registration, login (password/OTP/social), and password recovery. Designed to integrate with the B-Frontend React app.

## Development Commands

### Running the application

**Dev mode (H2 in-memory, zero setup):**
```bash
mvn spring-boot:run
```
Default profile is `dev`, runs on `http://localhost:8081`. OTP codes are printed to console and included in API responses (`debugOtp` field).

**Production mode (PostgreSQL):**
```bash
export DB_URL=jdbc:postgresql://localhost:5432/bgroceries
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=some-long-random-secret-at-least-32-chars
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Testing

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=AuthServiceTest

# Run a specific test method
mvn test -Dtest=AuthServiceTest#testRegister

# Skip tests during build
mvn clean install -DskipTests
```

### Building

```bash
# Clean and package
mvn clean package

# Compile only
mvn compile
```

## Architecture

### Authentication Flow

The system supports multiple authentication methods that converge into a single JWT-based session:

1. **Register + Password Login**: Phone-based registration with BCrypt-hashed passwords
2. **OTP Login**: Passwordless login via SMS OTP to registered phone numbers
3. **Social Login**: OAuth integration with Google, Facebook, and Telegram
4. **Password Recovery**: OTP-based password reset flow with short-lived reset tokens

All authentication methods return a JWT access token (24h expiry by default) in `AuthResponse`.

### User Identity Model

Users have multiple optional identity fields allowing flexible login methods:
- `phoneNumber` — normalized to `+855XXXXXXXXX` format (Cambodia), used for OTP flows
- `username` — unique login identifier, generated from social profiles if not provided
- `email` — used for Google/Facebook account linking
- `telegram` / `facebook` — social media handles
- `googleId` / `facebookId` / `telegramId` — stable provider IDs from verified OAuth tokens

Phone numbers are normalized via `PhoneUtil.normalize()` to handle user input variations (`012345678` → `+85512345678`). Social accounts are linked by provider ID, with email-based fallback for Google/Facebook.

### OTP Security

OTP codes are never stored in plaintext. The `OtpService`:
1. Generates a 6-digit code using `SecureRandom`
2. Stores only the BCrypt hash in the `otp_codes` table
3. Tracks attempts (max 5) and expiration (5 min)
4. Marks codes as `used=true` after successful verification to prevent reuse

OTP purposes (`LOGIN`, `RESET_PASSWORD`) are scoped separately in the database.

### Social Login Verification

When a `token` is provided in `SocialLoginRequest`, the backend cryptographically verifies it server-side before trusting the identity:

- **Gmail**: Verifies Google ID token (JWT) signature against Google's JWKS, checks `aud` matches configured client ID, ensures `email_verified=true`
- **Facebook**: Validates user access token via `https://graph.facebook.com/v21.0/debug_token` endpoint
- **Telegram**: Verifies Login Widget `auth` object using HMAC-SHA256 with `SHA256(bot_token)` as key

Verification is implemented in `SocialVerifier` interface with provider-specific implementations (`GoogleSocialVerifier`, `FacebookSocialVerifier`, `TelegramSocialVerifier`). If no `token` is provided, falls back to simulated demo mode (find-or-create by handle/email).

OAuth credentials are configured via environment variables (`GOOGLE_CLIENT_ID`, `FACEBOOK_APP_ID`, `FACEBOOK_APP_SECRET`, `TELEGRAM_BOT_TOKEN`) in `application.yml`.

### Security Configuration

`SecurityConfig` (src/main/java/.../config/SecurityConfig.java):
- All `/api/auth/**` endpoints are public
- All other endpoints require JWT authentication via `JwtAuthFilter`
- CORS is configured in `CorsConfig` with environment-based origins
- Sessions are stateless (no server-side session storage)
- Admin endpoints protected with `@PreAuthorize("hasRole('ADMIN')")` or path matcher `/api/admin/**`

JWT tokens carry `userId`, `role`, and `subject` (phone or username) claims. Reset tokens have `type: "RESET"` claim and shorter expiry (10 min).

### Package Structure

- `controller/` — REST endpoints, all return `ApiResponse<T>` wrapper
- `service/` — Business logic, transactional boundaries
- `repository/` — JPA repositories extending `JpaRepository`
- `entity/` — JPA entities with Lombok builders
- `dto/request/` — Input DTOs with `@Valid` Jakarta validation
- `dto/response/` — Output DTOs
- `security/` — JWT utilities, authentication filter, UserDetailsService
- `social/` — Social login verification (`SocialVerifier` implementations)
- `config/` — Spring configuration (security, CORS, data initialization)
- `exception/` — Custom exceptions with `GlobalExceptionHandler` for REST error responses
- `util/` — Utilities (phone normalization)

### Database Profiles

**dev** (default): H2 in-memory database at `jdbc:h2:mem:bgroceries`, accessible via `/h2-console`. Schema auto-created. Sample data loaded via `DataInitializer`.

**prod**: PostgreSQL via environment variables. JPA DDL mode is `update` to preserve existing data. Ensure database and credentials are configured before starting.

## Common Patterns

### Adding New Authenticated Endpoints

1. Create a new controller in `controller/` package
2. Add `@RestController`, `@RequestMapping`, and `@RequiredArgsConstructor`
3. Authenticated endpoints require `Authorization: Bearer <token>` header
4. Extract user from JWT via `@AuthenticationPrincipal` or parse token in service layer
5. Wrap responses in `ApiResponse.success()` or `ApiResponse.error()`

### Error Handling

Custom exceptions (`BadRequestException`, `NotFoundException`, `UnauthorizedException`, `ConflictException`) are caught by `GlobalExceptionHandler` and automatically converted to `ApiResponse` with appropriate HTTP status codes. Throw these exceptions directly from service methods.

### SMS Integration

`SmsService` interface has a console-only implementation (`ConsoleSmsServiceImpl`) that prints OTP codes to stdout. Replace with a real SMS gateway (Twilio, PlasGate) by implementing `SmsService` and marking it as `@Primary` or removing the console implementation.

## Configuration

Application settings are in `src/main/resources/application.yml`:
- JWT secrets and expiration times
- OTP length, expiry, max attempts, debug exposure
- Social OAuth credentials
- Database connection per profile
- Server port (default 8081)

Override with environment variables (e.g., `JWT_SECRET`, `DB_URL`) or Spring Boot properties (`-Dapp.otp.expiry-minutes=10`).

## Testing Flows

In dev mode, OTP codes are exposed in responses (`debugOtp` field) and console logs for testing without SMS gateway:

```
=== [SMS SIMULATION] OTP 483920 sent to +85512345678 ===
```

For social login testing without real OAuth:
- Omit the `token` field in `SocialLoginRequest` to use simulated demo mode
- Provide `identifier` (email/handle) or leave empty for stable demo accounts
- Real OAuth testing requires valid credentials in environment variables

## Notes

- Phone number changes are not currently supported; phone is immutable after registration
- User roles default to `USER`; promote to `ADMIN` via direct database update
- JWT tokens are not invalidated server-side (logout is client-side token deletion)
- OTP codes can be reused across attempts until verification or expiry/max attempts
- Social account linking happens on first login; subsequent logins match by provider ID
