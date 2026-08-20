# Telegram Login Integration - Setup Guide

## What Was Implemented

✅ **Backend (Spring Boot)**
- `LoginSession` entity for tracking Telegram login sessions
- `TelegramService` for handling bot interactions and JWT issuance
- `TelegramAuthController` - `/api/auth/telegram/init` and `/api/auth/telegram/status/{token}`
- `TelegramWebhookController` - `/api/telegram/webhook` for bot updates
- `User` entity updated with `telegram_user_id` (BIGINT) column
- `UserRepository` updated with `findByTelegramUserId()` method
- SecurityConfig updated to allow public access to Telegram endpoints
- application.yml updated with Telegram bot configuration

✅ **Frontend (React)**
- `TelegramLoginButton.jsx` - Basic standalone component
- `TelegramLoginButton-i18n.jsx` - Version with EN/KH i18n support
- Both located in `frontend-components/` directory

✅ **CORS Configuration**
- Existing CORS config already allows `localhost:5173` (Vite dev server)

## Manual Steps Required

### 1. Set Up Telegram Webhook (for local testing with ngrok)

**a) Install ngrok (if not already installed):**
```bash
# Download from https://ngrok.com/download
# Or use package manager:
winget install ngrok  # Windows
brew install ngrok    # Mac
```

**b) Start ngrok tunnel:**
```bash
ngrok http 8081
```

You'll see output like:
```
Forwarding  https://abc123.ngrok-free.app -> http://localhost:8081
```

**c) Register webhook with Telegram:**

Replace `<YOUR_NGROK_URL>` with the HTTPS URL from ngrok:

```bash
curl -X POST "https://api.telegram.org/bot8953064860:AAECU4pZPZaOMZ2SInNSotSNbZA9V8M-KWU/setWebhook" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://abc123.ngrok-free.app/api/telegram/webhook",
    "secret_token": "your-random-secret-here"
  }'
```

**Optional but recommended:** Set a webhook secret for security:
```bash
# Generate a random secret:
openssl rand -hex 32

# Then add to application.yml or environment variable:
export TELEGRAM_WEBHOOK_SECRET=<your-generated-secret>
```

**d) Verify webhook is set:**
```bash
curl "https://api.telegram.org/bot8953064860:AAECU4pZPZaOMZ2SInNSotSNbZA9V8M-KWU/getWebhookInfo"
```

### 2. Database Migration

Your User entity now has a new column `telegram_user_id`. Since you're using `ddl-auto=update` in dev mode, it should auto-create on next startup.

**For production (PostgreSQL):** Run this migration manually:

```sql
ALTER TABLE users ADD COLUMN telegram_user_id BIGINT UNIQUE;
CREATE INDEX idx_users_telegram_user_id ON users(telegram_user_id);
```

### 3. Environment Variables (Optional)

The following are already configured in `application.yml` with defaults, but you can override via environment variables:

```bash
# Bot credentials (already set in application.yml)
export TELEGRAM_BOT_TOKEN=8953064860:AAECU4pZPZaOMZ2SInNSotSNbZA9V8M-KWU
export TELEGRAM_BOT_USERNAME=BGroceriesbot

# Webhook security (recommended for production)
export TELEGRAM_WEBHOOK_SECRET=your-random-secret-here

# CORS (if you change your frontend URL)
# Currently configured for localhost:5173 in CorsConfig.java
```

### 4. Frontend Integration

**a) Copy the React component to your frontend project:**

```bash
# From your frontend project directory:
cp ../B-backend/frontend-components/TelegramLoginButton.jsx src/components/
# OR use the i18n version:
cp ../B-backend/frontend-components/TelegramLoginButton-i18n.jsx src/components/TelegramLoginButton.jsx
```

**b) Add to your login page:**

```jsx
import TelegramLoginButton from './components/TelegramLoginButton';

function LoginPage() {
  const handleSuccess = (jwt, userData) => {
    // Store JWT
    localStorage.setItem('authToken', jwt);
    
    // Update auth state
    // setUser(userData);
    
    // Redirect to dashboard
    navigate('/dashboard');
  };

  return (
    <div className="login-page">
      {/* Your existing phone/password login form */}
      
      <div className="divider">OR</div>
      
      <TelegramLoginButton
        apiBaseUrl="http://localhost:8081"  // Change for production
        onSuccess={handleSuccess}
        onError={(error) => console.error('Login failed:', error)}
      />
    </div>
  );
}
```

**c) Update apiBaseUrl for production:**

When deploying, change the `apiBaseUrl` prop to your production backend URL.

### 5. Testing the Flow

**a) Start the backend:**
```bash
mvn spring-boot:run
```

**b) Start ngrok (in separate terminal):**
```bash
ngrok http 8081
```

**c) Set webhook (using ngrok URL):**
```bash
curl -X POST "https://api.telegram.org/bot8953064860:AAECU4pZPZaOMZ2SInNSotSNbZA9V8M-KWU/setWebhook" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://YOUR-NGROK-URL.ngrok-free.app/api/telegram/webhook"}'
```

**d) Start your frontend:**
```bash
npm run dev  # or whatever your frontend start command is
```

**e) Test the login:**
1. Click "Login with Telegram" button
2. New tab opens with `https://t.me/BGroceriesbot?start=<token>`
3. Telegram app opens (or web version)
4. Tap "Start" button in the bot chat
5. Bot responds (you can customize the message later)
6. Frontend automatically detects completion and logs you in
7. JWT is stored and user is redirected

**f) Check backend logs:**
```
[INFO] Telegram login session initiated: <token>
[INFO] Processed /start command for user <telegram_user_id> with session <token>
[INFO] Telegram login completed for user <telegram_user_id> (session <token>)
[INFO] Created new Telegram user: <username> (ID: <telegram_user_id>)
```

### 6. Production Deployment

**a) Webhook URL:**
- Replace ngrok URL with your production domain
- Example: `https://api.bgroceries.com/api/telegram/webhook`

**b) Set webhook secret:**
```bash
export TELEGRAM_WEBHOOK_SECRET=$(openssl rand -hex 32)
```

**c) Update CORS origins in `CorsConfig.java`:**
```java
configuration.setAllowedOriginPatterns(List.of(
    "http://localhost:5173",    // Keep for local dev
    "https://localhost:5173",
    "https://bgroceries.com",   // Add your production domain
    "https://www.bgroceries.com"
));
```

**d) Update frontend apiBaseUrl:**
```jsx
<TelegramLoginButton
  apiBaseUrl="https://api.bgroceries.com"  // Production API
  onSuccess={handleSuccess}
/>
```

## Architecture Overview

### Login Flow

```
1. User clicks "Login with Telegram" on website
   ↓
2. Frontend → POST /api/auth/telegram/init
   ← Backend creates LoginSession, returns token
   ↓
3. Frontend opens https://t.me/BGroceriesbot?start={token}
   ↓
4. User taps "Start" in Telegram
   ↓
5. Telegram → POST /api/telegram/webhook (with /start command)
   ↓
6. Backend:
   - Finds LoginSession by token
   - Finds or creates User with telegram_user_id
   - Generates JWT using JwtUtil
   - Marks session as "COMPLETED" with JWT
   ↓
7. Frontend polls GET /api/auth/telegram/status/{token} (every 2s)
   ← Backend returns status="COMPLETED" with JWT
   ↓
8. Frontend stores JWT, logs user in, redirects to dashboard
```

### Database Schema

**users table (new column):**
```sql
telegram_user_id BIGINT UNIQUE  -- Telegram numeric user ID (e.g., 8953064860)
```

**login_sessions table (new):**
```sql
id                    BIGINT PRIMARY KEY
token                 VARCHAR(64) UNIQUE NOT NULL
telegram_user_id      BIGINT
telegram_username     VARCHAR(100)
telegram_first_name   VARCHAR(100)
status                VARCHAR(20) DEFAULT 'PENDING'
jwt_token             TEXT
expires_at            TIMESTAMP NOT NULL
created_at            TIMESTAMP NOT NULL
```

### User Creation for Telegram-Only Users

When a Telegram user logs in for the first time:
- Creates User with `telegram_user_id`, `telegram` (username), `username`, `fullName`
- Sets `loginProvider` = "telegram"
- Generates random password (user won't know it - Telegram login only)
- Sets `role` = "USER"
- Phone number is `NULL` (Telegram-only users don't have phone)

**IMPORTANT QUESTION:** How should Telegram-only users (no phone number) behave if phone number is required elsewhere in your app (e.g., for OTP, password reset, or checkout)? Should we:
1. Block them from those features until they add a phone?
2. Allow them to add a phone number later via profile settings?
3. Skip phone-based features entirely for Telegram users?

Please let me know your preference so I can add proper handling.

## Troubleshooting

### Webhook not receiving updates
```bash
# Check webhook status:
curl "https://api.telegram.org/bot8953064860:AAECU4pZPZaOMZ2SInNSotSNbZA9V8M-KWU/getWebhookInfo"

# If stuck, delete and re-set:
curl -X POST "https://api.telegram.org/bot8953064860:AAECU4pZPZaOMZ2SInNSotSNbZA9V8M-KWU/deleteWebhook"
```

### Polling timeout
- Default is 5 minutes (150 attempts × 2s)
- Adjust `MAX_POLL_ATTEMPTS` in `TelegramLoginButton.jsx` if needed

### Session expired
- Login sessions expire after 5 minutes
- Adjust in `TelegramService.createLoginSession()` if needed:
  ```java
  LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10); // Change from 5 to 10
  ```

### CORS errors
- Ensure your frontend origin is in `CorsConfig.java`
- Check that backend is running on port 8081
- Verify frontend is calling the correct API URL

## Bot Customization (Optional)

The current implementation just receives the `/start` command. You can customize the bot's response by:

**a) Using Telegram Bot API to send messages:**

Add to `TelegramService.processStartCommand()`:

```java
private void sendTelegramMessage(Long chatId, String text) {
    String url = String.format(
        "https://api.telegram.org/bot%s/sendMessage",
        botToken  // inject from @Value("${telegram.bot.token}")
    );
    
    RestTemplate restTemplate = new RestTemplate();
    Map<String, Object> body = Map.of(
        "chat_id", chatId,
        "text", text
    );
    
    try {
        restTemplate.postForEntity(url, body, String.class);
    } catch (Exception e) {
        log.error("Failed to send Telegram message: {}", e.getMessage());
    }
}

// Then call it:
sendTelegramMessage(telegramUserId, "✅ Login successful! You can close this chat and return to the website.");
```

**b) Set bot commands via BotFather:**
```
/setcommands
@BGroceriesbot
start - Login to B'Groceries
help - Get help
```

## Summary

✅ Backend integration complete and compiled successfully
✅ Frontend components ready in `frontend-components/`
✅ Configuration files updated with Telegram settings
✅ Security config updated to allow public access to Telegram endpoints

**Still needed:**
1. Set up ngrok and register webhook URL
2. Copy React component to your frontend project
3. Add component to your login page
4. Test the complete flow
5. Answer the question about Telegram-only users and phone numbers

All code follows your existing patterns (JwtUtil, ApiResponse, Lombok builders, etc.) and integrates seamlessly with your current authentication system.
