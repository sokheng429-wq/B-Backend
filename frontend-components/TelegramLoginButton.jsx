import React, { useState } from 'react';

const TelegramLoginButton = ({ onSuccess, onError, apiBaseUrl = 'http://localhost:8081' }) => {
  const [isPolling, setIsPolling] = useState(false);
  const [error, setError] = useState(null);

  const BOT_USERNAME = 'BGroceriesbot';
  const POLL_INTERVAL_MS = 2000;
  const MAX_POLL_ATTEMPTS = 150; // 5 minutes (150 * 2s)

  const handleTelegramLogin = async () => {
    try {
      setError(null);
      setIsPolling(true);

      // Step 1: Initialize login session
      const initResponse = await fetch(`${apiBaseUrl}/api/auth/telegram/init`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });

      if (!initResponse.ok) {
        throw new Error('Failed to initialize Telegram login');
      }

      const initData = await initResponse.json();
      if (!initData.success || !initData.data?.token) {
        throw new Error('Invalid response from server');
      }

      const sessionToken = initData.data.token;

      // Step 2: Open Telegram bot in new tab with deep link
      const deepLink = `https://t.me/${BOT_USERNAME}?start=${sessionToken}`;
      window.open(deepLink, '_blank');

      // Step 3: Start polling for completion
      let attempts = 0;
      const pollInterval = setInterval(async () => {
        attempts++;

        if (attempts > MAX_POLL_ATTEMPTS) {
          clearInterval(pollInterval);
          setIsPolling(false);
          const timeoutError = 'Login timeout - please try again';
          setError(timeoutError);
          if (onError) onError(new Error(timeoutError));
          return;
        }

        try {
          const statusResponse = await fetch(
            `${apiBaseUrl}/api/auth/telegram/status/${sessionToken}`,
            { method: 'GET' }
          );

          if (!statusResponse.ok) {
            throw new Error('Failed to check login status');
          }

          const statusData = await statusResponse.json();

          if (statusData.success && statusData.data) {
            const { status, jwt } = statusData.data;

            if (status === 'COMPLETED' && jwt) {
              clearInterval(pollInterval);
              setIsPolling(false);

              // Store JWT and trigger success callback
              localStorage.setItem('authToken', jwt);
              if (onSuccess) onSuccess(jwt, statusData.data);
            } else if (status === 'EXPIRED') {
              clearInterval(pollInterval);
              setIsPolling(false);
              const expiredError = 'Session expired - please try again';
              setError(expiredError);
              if (onError) onError(new Error(expiredError));
            }
          }
        } catch (pollError) {
          console.error('Polling error:', pollError);
        }
      }, POLL_INTERVAL_MS);

    } catch (err) {
      console.error('Telegram login error:', err);
      setIsPolling(false);
      setError(err.message || 'Login failed');
      if (onError) onError(err);
    }
  };

  return (
    <div className="telegram-login-container">
      <button
        onClick={handleTelegramLogin}
        disabled={isPolling}
        className="telegram-login-button"
        style={{
          backgroundColor: '#0088cc',
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
        {isPolling ? 'Waiting for Telegram...' : 'Login with Telegram'}
      </button>

      {isPolling && (
        <p style={{ marginTop: '8px', fontSize: '14px', color: '#666' }}>
          Check your Telegram and tap "Start" in the bot chat
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
