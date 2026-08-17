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
