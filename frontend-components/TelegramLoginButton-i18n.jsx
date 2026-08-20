import React, { useState, useContext } from 'react';
// import { LanguageContext } from './contexts/LanguageContext'; // Adjust path to your actual LanguageContext
// import { useAuth } from './contexts/AuthContext'; // Adjust path to your actual AuthContext

/**
 * Telegram Login Button with internationalization (EN/KH) and auth context integration.
 *
 * Usage:
 * 1. Uncomment the imports above and adjust paths to match your project structure
 * 2. Replace the mock useLanguage and useAuth hooks with your actual implementations
 * 3. Adjust button styling to match your navy/green/orange palette
 * 4. Place this component on your login page next to phone/password login form
 */

// Mock hooks - REPLACE these with your actual implementations
const useLanguage = () => {
  const [language] = useState('en'); // or use your actual LanguageContext
  const t = (key) => {
    const translations = {
      en: {
        'telegram.login': 'Login with Telegram',
        'telegram.waiting': 'Waiting for Telegram...',
        'telegram.instruction': 'Check your Telegram and tap "Start" in the bot chat',
        'telegram.timeout': 'Login timeout - please try again',
        'telegram.expired': 'Session expired - please try again',
        'telegram.failed': 'Login failed',
        'telegram.init_failed': 'Failed to initialize Telegram login'
      },
      kh: {
        'telegram.login': 'ចូលដោយប្រើ Telegram',
        'telegram.waiting': 'កំពុងរង់ចាំ Telegram...',
        'telegram.instruction': 'ពិនិត្យ Telegram របស់អ្នក ហើយចុច "ចាប់ផ្តើម" ក្នុងការជជែកបូត',
        'telegram.timeout': 'អស់ពេល - សូមព្យាយាមម្តងទៀត',
        'telegram.expired': 'សម័យផុតកំណត់ - សូមព្យាយាមម្តងទៀត',
        'telegram.failed': 'ការចូលបានបរាជ័យ',
        'telegram.init_failed': 'មិនអាចចាប់ផ្តើមការចូល Telegram បានទេ'
      }
    };
    return translations[language]?.[key] || key;
  };
  return { t };
};

const useAuth = () => {
  // REPLACE with your actual auth context hook
  const login = (token, userData) => {
    console.log('Login successful:', userData);
    localStorage.setItem('authToken', token);
    // Trigger your app's login flow, redirect to dashboard, etc.
  };
  return { login };
};

const TelegramLoginButton = ({ apiBaseUrl = 'http://localhost:8081' }) => {
  const [isPolling, setIsPolling] = useState(false);
  const [error, setError] = useState(null);
  const { t } = useLanguage();
  const { login } = useAuth();

  const BOT_USERNAME = 'BGroceriesbot';
  const POLL_INTERVAL_MS = 2000;
  const MAX_POLL_ATTEMPTS = 150; // 5 minutes

  const handleTelegramLogin = async () => {
    try {
      setError(null);
      setIsPolling(true);

      const initResponse = await fetch(`${apiBaseUrl}/api/auth/telegram/init`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });

      if (!initResponse.ok) {
        throw new Error(t('telegram.init_failed'));
      }

      const initData = await initResponse.json();
      if (!initData.success || !initData.data?.token) {
        throw new Error(t('telegram.init_failed'));
      }

      const sessionToken = initData.data.token;
      const deepLink = `https://t.me/${BOT_USERNAME}?start=${sessionToken}`;
      window.open(deepLink, '_blank');

      let attempts = 0;
      const pollInterval = setInterval(async () => {
        attempts++;

        if (attempts > MAX_POLL_ATTEMPTS) {
          clearInterval(pollInterval);
          setIsPolling(false);
          setError(t('telegram.timeout'));
          return;
        }

        try {
          const statusResponse = await fetch(
            `${apiBaseUrl}/api/auth/telegram/status/${sessionToken}`,
            { method: 'GET' }
          );

          if (!statusResponse.ok) throw new Error(t('telegram.failed'));

          const statusData = await statusResponse.json();

          if (statusData.success && statusData.data) {
            const { status, jwt } = statusData.data;

            if (status === 'COMPLETED' && jwt) {
              clearInterval(pollInterval);
              setIsPolling(false);
              login(jwt, statusData.data);
            } else if (status === 'EXPIRED') {
              clearInterval(pollInterval);
              setIsPolling(false);
              setError(t('telegram.expired'));
            }
          }
        } catch (pollError) {
          console.error('Polling error:', pollError);
        }
      }, POLL_INTERVAL_MS);

    } catch (err) {
      console.error('Telegram login error:', err);
      setIsPolling(false);
      setError(err.message || t('telegram.failed'));
    }
  };

  return (
    <div className="telegram-login-container">
      <button
        onClick={handleTelegramLogin}
        disabled={isPolling}
        className="telegram-login-button"
        style={{
          // Adjust these colors to match your navy/green/orange palette
          backgroundColor: '#0088cc', // Telegram blue - replace with your navy
          color: 'white',
          padding: '12px 24px',
          border: 'none',
          borderRadius: '6px',
          fontSize: '16px',
          fontWeight: '500',
          cursor: isPolling ? 'not-allowed' : 'pointer',
          opacity: isPolling ? 0.6 : 1,
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          transition: 'opacity 0.2s'
        }}
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15 1.58-.8 5.42-1.13 7.19-.14.75-.42 1-.68 1.03-.58.05-1.02-.38-1.58-.75-.88-.58-1.38-.94-2.23-1.5-.99-.65-.35-1.01.22-1.59.15-.15 2.71-2.48 2.76-2.69a.2.2 0 00-.05-.18c-.06-.05-.14-.03-.21-.02-.09.02-1.49.95-4.22 2.79-.4.27-.76.41-1.08.4-.36-.01-1.04-.2-1.55-.37-.63-.2-1.12-.31-1.08-.66.02-.18.27-.36.74-.55 2.92-1.27 4.86-2.11 5.83-2.51 2.78-1.16 3.35-1.36 3.73-1.36.08 0 .27.02.39.12.1.08.13.19.14.27-.01.06.01.24 0 .38z"/>
        </svg>
        {isPolling ? t('telegram.waiting') : t('telegram.login')}
      </button>

      {isPolling && (
        <p style={{ marginTop: '8px', fontSize: '14px', color: '#666' }}>
          {t('telegram.instruction')}
        </p>
      )}

      {error && (
        <p style={{ marginTop: '8px', fontSize: '14px', color: '#d32f2f' }}>
          {error}
        </p>
      )}
    </div>
  );
};

export default TelegramLoginButton;
