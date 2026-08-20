# B'Groceries — Auth Backend

Spring Boot 3.3 (Java 17) backend covering **only auth** for now:

- Register with phone number + password
- Login with phone number + password
- Login with OTP (sent to phone number)
- Forgot password → OTP by phone → reset password

Built to match the `B-Frontend` React app's `Login.jsx`, `Register.jsx`, and
`Forgotpassword.jsx` field names (`fullName`, `phoneNumber`, `password`,
`confirmPassword`, `otp`).

## Stack

- Java 17, Spring Boot 3.3
- Spring Security + JWT (jjwt 0.12)
- Spring Data JPA
- H2 in-memory DB for **dev** (zero setup), PostgreSQL for **prod**
- BCrypt password hashing (also used to hash OTP codes at rest)
- SMS sending is behind an `SmsService` interface — currently a console
  logger (`ConsoleSmsServiceImpl`) since no gateway is wired up yet.

## Run locally (dev profile, H2, no setup needed)

```bash
mvn spring-boot:run
```

Runs on `http://localhost:8080`, profile `dev` is active by default
(see `application.yml`). In dev mode, OTP codes are **also returned in the
API response** (`debugOtp` field) and printed to the console log, so you can
test the whole flow without a real SMS gateway:

```
=== [SMS SIMULATION] OTP 483920 sent to +85512345678 ===
```

H2 console (optional, to inspect data): `http://localhost:8080/h2-console`
JDBC URL: `jdbc:h2:mem:bgroceries`, user `sa`, empty password.

## Run with PostgreSQL (prod profile)

```bash
export DB_URL=jdbc:postgresql://localhost:5432/bgroceries
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=some-long-random-secret-at-least-32-chars
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

In `prod`, `debugOtp` is **not** returned — swap `ConsoleSmsServiceImpl` for a
real `SmsService` implementation (Twilio, PlasGate, or another Cambodian SMS
gateway) before going live.

## Import into IntelliJ

Open the `backend` folder as a Maven project (IntelliJ will detect `pom.xml`
automatically), let it download dependencies, then run `BackendApplication`.

---

## API Reference

Base URL: `http://localhost:8080/api/auth`
All responses are wrapped as:
```json
{ "success": true, "message": "...", "data": { } }
```

### 1. Register
`POST /register`
```json
{
  "fullName": "Sok Heng",
  "phoneNumber": "012345678",
  "password": "mypassword123",
  "confirmPassword": "mypassword123"
}
```
→ `201 Created`, returns `{ token, tokenType, user }` (auto-logs in after register).

### 2. Login with password
`POST /login`
```json
{ "phoneNumber": "012345678", "password": "mypassword123" }
```
→ `200 OK`, returns `{ token, tokenType, user }`.

### 3. Login with OTP — step 1: send code
`POST /login/otp/send`
```json
{ "phoneNumber": "012345678" }
```
→ `200 OK`, returns `{ phoneNumber, expiresInSeconds, debugOtp }`
(`debugOtp` only in dev). Fails with 404 if no account exists for that number.

### 4. Login with OTP — step 2: verify code
`POST /login/otp/verify`
```json
{ "phoneNumber": "012345678", "otp": "483920" }
```
→ `200 OK`, returns `{ token, tokenType, user }`.

### 5. Forgot password — step 1: send code
`POST /forgot-password/send-otp`
```json
{ "phoneNumber": "012345678" }
```
→ `200 OK`, returns `{ phoneNumber, expiresInSeconds, debugOtp }`.

### 6. Forgot password — step 2: verify code
`POST /forgot-password/verify-otp`
```json
{ "phoneNumber": "012345678", "otp": "483920" }
```
→ `200 OK`, returns `{ resetToken, expiresInSeconds }`.
`resetToken` is a short-lived JWT (10 min default) — keep it in the frontend's
state/localStorage for the last step below.

### 7. Forgot password — step 3: reset password
`POST /forgot-password/reset`
```json
{
  "resetToken": "<token from step 6>",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```
→ `200 OK`, `{ success: true, message: "Password reset successfully" }`.

### 8. Social login (Google / Telegram / Facebook)
`POST /social`
```json
{ "provider": "gmail", "token": "<Google ID token>" }
```
→ `200 OK`, returns `{ token, tokenType, user }`.

`token` is the provider-issued credential, verified **server-side**:

- **gmail** — a Google ID token (JWT) from Google Identity Services. The backend
  checks the signature against Google's JWKS, `aud` matches the configured client id,
  and `email_verified=true`.
- **facebook** — a user access token from the Facebook JS SDK. Verified against
  `https://graph.facebook.com/{version}/debug_token` and the profile fetched.
- **telegram** — the Login Widget `auth` object serialized as JSON, e.g.
  `{"id":123,"first_name":"X","last_name":"Y","username":"xy","auth_date":1515957854,"hash":"..."}`.
  Verified with the official HMAC-SHA256 algorithm (keyed by `SHA256(bot_token)`).

Accounts are linked by the provider's stable id (`google_id` / `facebook_id` /
`telegram_id`); a provider-verified email also links an existing account. First login
creates the account. If `token` is absent, the legacy simulated demo behavior applies
(stable demo account per provider, or the optional `identifier`).

Credentials are configured via env vars (`GOOGLE_CLIENT_ID`, `FACEBOOK_APP_ID`,
`FACEBOOK_APP_SECRET`, `TELEGRAM_BOT_TOKEN`). Until real values are set, any request
with a `token` fails with `401 Invalid provider token`.

---

## Notes

- Phone numbers are normalized internally to `+855XXXXXXXXX` regardless of
  whether the user types `012345678`, `85512345678`, or `+85512345678` — they
  all map to the same account.
- OTP codes are 6 digits, expire after 5 minutes, max 5 wrong attempts before
  a new code is required (all configurable in `application.yml`).
- OTP codes are hashed with BCrypt before being stored — never stored in plain text.
- All `/api/auth/**` endpoints are public; everything else requires
  `Authorization: Bearer <token>` once you add more protected endpoints later
  (e.g. `/api/products`, `/api/orders`).
