import { useState } from 'react';

const TELEGRAM_BOT_USERNAME = 'BGroceriesbot';
const API_BASE_URL = 'http://localhost:8081';
const POLL_INTERVAL_MS = 2000;
const MAX_POLL_ATTEMPTS = 150; // 5 minutes

/**
 * Telegram login button using Bot Deep Link method.
 *
 * Flow:
 * 1. User clicks button
 * 2. Backend creates login session and returns token
 * 3. Opens https://t.me/BGroceriesbot?start={token} in new tab
 * 4. User taps "Start" in Telegram bot
 * 5. Bot webhook processes /start command
 * 6. Frontend polls /api/auth/telegram/status/{token} until completed
 * 7. Receives JWT and logs user in
 */
export function useTelegramLogin({ onAuth, onError }) {
  const [isPolling, setIsPolling] = useState(false);
  const [error, setError] = useState(null);

  const handleTelegramLogin = async () => {
    try {
      setError(null);
      setIsPolling(true);

      // Step 1: Initialize login session
      const initResponse = await fetch(`${API_BASE_URL}/api/auth/telegram/init`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });

      if (!initResponse.ok) {
        throw new Error('Failed to initialize Telegram login');
      }

      const initData = await initResponse.json();
      if (!initData.success || !initData.data?.token) {
        throw new Error(initData.message || 'Invalid response from server');
      }

      const sessionToken = initData.data.token;
      console.log('[Telegram Login] Session created:', sessionToken);

      // Step 2: Open Telegram bot with deep link
      const deepLink = `https://t.me/${TELEGRAM_BOT_USERNAME}?start=${sessionToken}`;
      window.open(deepLink, '_blank');

      // Step 3: Poll for completion
      let attempts = 0;
      const pollInterval = setInterval(async () => {
        attempts++;

        if (attempts > MAX_POLL_ATTEMPTS) {
          clearInterval(pollInterval);
          setIsPolling(false);
          const timeoutError = new Error('Login timeout - please try again');
          setError(timeoutError.message);
          onError?.(timeoutError);
          return;
        }

        try {
          const statusResponse = await fetch(
            `${API_BASE_URL}/api/auth/telegram/status/${sessionToken}`
          );

          if (!statusResponse.ok) {
            throw new Error('Failed to check login status');
          }

          const statusData = await statusResponse.json();

          if (statusData.success && statusData.data) {
            const { status, jwt, telegramUserId, telegramUsername } = statusData.data;

            if (status === 'COMPLETED' && jwt) {
              clearInterval(pollInterval);
              setIsPolling(false);
              console.log('[Telegram Login] Success:', { telegramUserId, telegramUsername });

              // Call onAuth with Telegram user data
              onAuth({
                token: jwt,
                telegramUserId,
                telegramUsername,
                provider: 'telegram'
              });
            } else if (status === 'EXPIRED') {
              clearInterval(pollInterval);
              setIsPolling(false);
              const expiredError = new Error('Session expired - please try again');
              setError(expiredError.message);
              onError?.(expiredError);
            }
          }
        } catch (pollError) {
          console.error('[Telegram Login] Polling error:', pollError);
        }
      }, POLL_INTERVAL_MS);

    } catch (err) {
      console.error('[Telegram Login] Error:', err);
      setIsPolling(false);
      setError(err.message || 'Login failed');
      onError?.(err);
    }
  };

  return {
    handleTelegramLogin,
    isPolling,
    error,
    // Return empty ref for compatibility with your existing button structure
    telegramButtonRef: { current: null }
  };
}
