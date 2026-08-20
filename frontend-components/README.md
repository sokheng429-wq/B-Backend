# Telegram Login Frontend Components

These are the React components for Telegram login integration with B'Groceries.

## Files

1. **TelegramLoginButton.jsx** - Basic standalone version with no dependencies
2. **TelegramLoginButton-i18n.jsx** - Version with EN/KH i18n support and auth context integration

## Integration Steps

### Option 1: Basic Version (TelegramLoginButton.jsx)

Use this if you want a simple drop-in component:

```jsx
import TelegramLoginButton from './components/TelegramLoginButton';

function LoginPage() {
  const handleSuccess = (jwt, userData) => {
    console.log('Login successful!', userData);
    // Store JWT, redirect to dashboard, etc.
  };

  const handleError = (error) => {
    console.error('Login failed:', error);
  };

  return (
    <div>
      <TelegramLoginButton
        apiBaseUrl="http://localhost:8081"
        onSuccess={handleSuccess}
        onError={handleError}
      />
    </div>
  );
}
```

### Option 2: i18n Version (TelegramLoginButton-i18n.jsx)

Use this if you have LanguageContext and AuthContext:

1. Open `TelegramLoginButton-i18n.jsx`
2. Uncomment the imports at the top and adjust paths to your contexts
3. Remove or replace the mock `useLanguage` and `useAuth` hooks with your real implementations
4. Adjust the button styling colors to match your navy/green/orange palette
5. Import and use on your login page:

```jsx
import TelegramLoginButton from './components/TelegramLoginButton-i18n';

function LoginPage() {
  return (
    <div className="login-page">
      <form>
        {/* Your existing phone/password login form */}
      </form>
      
      <div className="divider">OR</div>
      
      <TelegramLoginButton />
    </div>
  );
}
```

## Environment Configuration

Make sure your API base URL matches your backend:
- Development: `http://localhost:8081`
- Production: Update `apiBaseUrl` prop to your production API domain

## Flow

1. User clicks "Login with Telegram" button
2. Backend creates a login session and returns a token
3. Opens `https://t.me/BGroceriesbot?start={token}` in new tab
4. User taps "Start" in Telegram bot
5. Bot sends `/start {token}` to webhook
6. Frontend polls `/api/auth/telegram/status/{token}` every 2 seconds
7. When status becomes "COMPLETED", receives JWT and logs user in
8. Auto-redirects to dashboard (implement in your `onSuccess` or `login` handler)

## Customization

### Styling
The button uses inline styles for portability. To match your design system:
- Replace `backgroundColor: '#0088cc'` with your navy color
- Adjust padding, border-radius, and font styles
- Or remove inline styles and use your CSS classes

### Bot Username
If you change your Telegram bot username, update the constant:
```javascript
const BOT_USERNAME = 'YourNewBotUsername';
```

### Polling Configuration
Adjust these constants if needed:
```javascript
const POLL_INTERVAL_MS = 2000; // Check every 2 seconds
const MAX_POLL_ATTEMPTS = 150; // 5 minute timeout (150 * 2s)
```

## Translations (i18n version only)

The i18n version includes EN/KH translations. Add more languages in the `translations` object:

```javascript
const translations = {
  en: { ... },
  kh: { ... },
  fr: {
    'telegram.login': 'Se connecter avec Telegram',
    // ... more translations
  }
};
```
