## Objective
Replace the simulated social login in the Spring Boot 3.3.4 auth backend with cryptographically verified Google / Facebook / Telegram login, while preserving the current demo fallback and the `{provider, identifier}` API contract.

## Stack Decision (detected, respected — no changes)
Java 17 + Spring Boot 3.3.4, Maven, Lombok, JPA/Hibernate (H2 dev / Neon prod), jjwt 0.12.6, Spring Security. Zero new dependencies: `RestClient` (spring-web 6.1) for HTTP, jjwt `Jwks`/`KeyLocator` for Google JWKS, Jackson for JSON, JDK `javax.crypto` for Telegram HMAC. Do NOT add `oauth2-client` or any `spring-security-web.filter` class (corrupted jars on this machine).

## Findings (provider flows validated against official docs)
- **Google**: JWKS URL `https://www.googleapis.com/oauth2/v3/certs` confirmed. Claims: `iss` in {accounts.google.com, https://accounts.google.com}, `aud` == client-id, `email_verified == true` (required for email matching). jjwt 0.12 API confirmed: `Jwks.parser().build().parse()` -> `JwkSet`, `Jwks.keyLocator(jwkSet)` -> `KeyLocator`. **Correction to your design**: `aud` can be a String or array — check `claims.get("aud")` manually, don't rely on `Claims.getAudience()`.
- **Facebook**: `debug_token?input_token=...&access_token={app_id}|{app_secret}` confirmed; app token pipe must be URL-encoded `%7C` (use `UriComponentsBuilder.queryParam`). Check `data.is_valid` AND `data.app_id`. Profile via `/{user_id}?fields=id,name,email` with the user token. Email is returned only if the user granted `email` permission — handle absent email.
- **Telegram**: legacy Login Widget algorithm confirmed from official archived docs: `secret_key = SHA256(bot_token)`; `data_check_string` = all `key=value` except `hash`, sorted alphabetically, joined `\n`; compare lowercase-hex HMAC-SHA256 constant-time; auth_date "can additionally be checked" — use the 24h convention + ~5 min future-skew rejection. `id` may exceed int — keep as String.

## Approach
Add nullable unique `google_id` / `facebook_id` / `telegram_id` columns and find-by-provider-id first; fall back to email linking **only** when the email is provider-verified (Google `email_verified=true`; Facebook email via granted permission), and stamp the provider-id on the linked row. Email-only matching is rejected: Telegram carries no email, Facebook email is optional, and provider ids are the stable unique key. Telegram auth object travels as a JSON string in a new nullable `token` field (one opaque credential field; `identifier` remains for the demo path only and is ignored when `token` is present). Config gate: blank/placeholder `app.social.*` creds fail safe with 401 before any network/crypto work; all verification failures return an opaque 401.

## Implementation Plan (ordered)
1. `application.yml` — add `app.social.google.client-id`, `app.social.facebook.app-id` / `app-secret` / `graph-version` (default `v21.0`), `app.social.telegram.bot-token`, all `${ENV_VAR:}` driven.
2. `SocialLoginRequest.java` — add nullable `String token`.
3. `entity/User.java` — add `googleId`, `facebookId`, `telegramId` (unique, nullable). `ddl-auto: update` migrates H2 and Neon.
4. `UserRepository.java` — add `findByGoogleId` / `findByFacebookId` / `findByTelegramId`.
5. New `social/` package: `SocialProfile` record, `SocialVerifier` interface (`provider()` + `verify(String token)`), `GoogleSocialVerifier` (provider()="gmail" — matches the API contract; RestClient with 5s timeouts; ~1h JWKS cache via volatile fields; refresh-once-on-failure), `FacebookSocialVerifier`, `TelegramSocialVerifier`.
6. `AuthService.java` — inject `List<SocialVerifier>`; `socialLogin` branches: token present -> pick verifier, verify, `findOrCreateFromProfile`; else existing demo path unchanged.
7. Tests (dev/H2): Telegram known-answer HMAC test (fixed token+auth JSON, expected hex precomputed externally and hardcoded; tamper/expiry cases); Google test with locally-generated RSA keypair, hand-built JWKS served via `MockRestServiceServer`, signed tokens covering valid / wrong aud / wrong iss / unverified-email; Facebook `MockRestServiceServer` stubs (valid, is_valid=false, wrong app_id, missing email); `AuthServiceSocialTest` with mocked verifiers (create, email-link, provider-id relink, demo regression, fail-safe).
8. Verify: `mvn clean test`, then dev-run + `POST /api/auth/social` `{provider:"gmail"}` without token -> demo account regression.

## Needed Collaborators
`backend` only (frontend is a separate repo; nothing here touches it).

## Critical Files
- D:\1.B.Groceries\Backend\B-backend\src\main\java\com\bgroceries\backend\service\AuthService.java
- D:\1.B.Groceries\Backend\B-backend\src\main\java\com\bgroceries\backend\dto\request\SocialLoginRequest.java
- D:\1.B.Groceries\Backend\B-backend\src\main\java\com\bgroceries\backend\entity\User.java
- D:\1.B.Groceries\Backend\B-backend\src\main\java\com\bgroceries\backend\repository\UserRepository.java
- D:\1.B.Groceries\Backend\B-backend\src\main\resources\application.yml
- New: src/main/java/com/bgroceries/backend/social/* (5 files)

## Risks & Open Questions
- **Email auto-link takeover**: manual register never verifies email ownership, so a provider-verified email matching an attacker-registered row would link to it. Mitigated (verified-email-only); stricter 409-conflict mode is a later option.
- Google `aud` array handling; Facebook missing-email accounts (fine — email nullable); Telegram optional `username`/`photo_url`.
- Verify column migration on Neon (prod) after deploy.

Note: plan mode blocks writing to the repo, so the shared brief is below, ready to write to `D:\1.B.Groceries\Backend\B-backend\.rayu\swarm\shared.json` on approval:
```json
{"goal":"Real social login (Google/Facebook/Telegram): verify provider tokens server-side, find-or-create linked users, keep demo fallback","stack":"java 17, spring boot 3.3.4, maven, jpa/h2+neon, jjwt 0.12, spring security","flow":"Frontend POSTs provider credential as token to POS
…[truncated]
