# Fix: OAuth2 Stuck on "Signing you in" Screen

## Problem
- ✅ OAuth2 login works (user name appears next to logout)
- ✅ Token is received and stored
- ❌ Frontend stuck on "signing you in" loading screen
- ❌ Not redirecting to homepage

---

## Solution: Fix OAuth2Redirect Component

Replace your `OAuth2Redirect.jsx` with this corrected version:

### **File: `src/pages/OAuth2Redirect.jsx`**

```jsx
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

export default function OAuth2Redirect() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('processing');

  useEffect(() => {
    const token = searchParams.get('token');
    const error = searchParams.get('error');

    console.log('OAuth2Redirect - Token:', token ? 'Received' : 'Missing');
    console.log('OAuth2Redirect - Error:', error || 'None');

    if (token) {
      try {
        // Store JWT token
        localStorage.setItem('accessToken', token);
        console.log('✅ Token stored in localStorage');
        
        // Decode token to get user info (optional)
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          localStorage.setItem('userId', payload.userId);
          localStorage.setItem('role', payload.role);
          console.log('✅ User info stored:', payload);
        } catch (err) {
          console.warn('Could not decode token:', err);
        }
        
        setStatus('success');
        
        // CRITICAL: Navigate immediately, don't wait
        console.log('🔄 Redirecting to homepage...');
        
        // Use setTimeout to ensure state updates are processed
        setTimeout(() => {
          navigate('/', { replace: true });
        }, 100);
        
      } catch (err) {
        console.error('❌ Error processing token:', err);
        setStatus('error');
        setTimeout(() => {
          navigate('/login', { replace: true });
        }, 2000);
      }
    } else if (error) {
      console.error('❌ OAuth2 error:', decodeURIComponent(error));
      setStatus('error');
      setTimeout(() => {
        navigate('/login', { 
          replace: true,
          state: { error: decodeURIComponent(error) }
        });
      }, 2000);
    } else {
      console.error('❌ No token or error in URL');
      setStatus('error');
      setTimeout(() => {
        navigate('/login', { replace: true });
      }, 2000);
    }
  }, [searchParams, navigate]);

  // Simple loading screen - don't block navigation with complex UI
  return (
    <div style={styles.container}>
      <div style={styles.card}>
        {status === 'processing' && (
          <>
            <div style={styles.spinner}></div>
            <h2>Signing you in...</h2>
            <p>Please wait</p>
          </>
        )}
        
        {status === 'success' && (
          <>
            <div style={styles.checkmark}>✓</div>
            <h2>Success!</h2>
            <p>Redirecting to homepage...</p>
          </>
        )}
        
        {status === 'error' && (
          <>
            <div style={styles.errorIcon}>✗</div>
            <h2>Login failed</h2>
            <p>Redirecting back to login...</p>
          </>
        )}
      </div>
    </div>
  );
}

const styles = {
  container: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  },
  card: {
    background: 'white',
    borderRadius: '16px',
    padding: '40px',
    textAlign: 'center',
    boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
    minWidth: '320px',
  },
  spinner: {
    width: '50px',
    height: '50px',
    border: '4px solid #f3f3f3',
    borderTop: '4px solid #667eea',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite',
    margin: '0 auto 20px',
  },
  checkmark: {
    fontSize: '60px',
    color: '#22c55e',
    marginBottom: '20px',
  },
  errorIcon: {
    fontSize: '60px',
    color: '#ef4444',
    marginBottom: '20px',
  },
};
```

---

## Alternative: Even Simpler Version (If Above Doesn't Work)

If the above still doesn't work, try this **minimal** version that navigates immediately:

```jsx
import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

export default function OAuth2Redirect() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const token = searchParams.get('token');
    
    if (token) {
      localStorage.setItem('accessToken', token);
      console.log('Token stored, redirecting to homepage...');
      
      // Navigate immediately - no delays, no state updates
      navigate('/', { replace: true });
    } else {
      console.error('No token found');
      navigate('/login', { replace: true });
    }
  }, [searchParams, navigate]);

  // Minimal UI - don't interfere with navigation
  return <div>Redirecting...</div>;
}
```

---

## Check Your Router Configuration

Make sure your `App.jsx` has the route configured correctly:

```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import OAuth2Redirect from './pages/OAuth2Redirect';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
        <Route path="/" element={<HomePage />} />
        {/* Other routes */}
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

**Make sure:**
- ✅ Path is exactly `/oauth2/redirect` (matches backend config)
- ✅ No wrapping with `ProtectedRoute` on the redirect route
- ✅ `BrowserRouter` is used (not `HashRouter`)

---

## Check Browser Console

Open browser console (F12) and look for:

```
OAuth2Redirect - Token: Received
✅ Token stored in localStorage
✅ User info stored: {userId: 21, role: "USER", ...}
🔄 Redirecting to homepage...
```

If you see these logs but still no redirect, the issue is with your Router setup.

---

## Common Issues & Fixes

### Issue 1: Route Not Found
**Symptom:** Console shows `No routes matched location "/oauth2/redirect"`

**Fix:** Add the route in `App.jsx`:
```jsx
<Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
```

### Issue 2: Protected Route Blocking
**Symptom:** Redirect happens but immediately goes back to login

**Fix:** Don't wrap `/oauth2/redirect` with `ProtectedRoute`:
```jsx
// ❌ WRONG
<Route path="/oauth2/redirect" element={<ProtectedRoute><OAuth2Redirect /></ProtectedRoute>} />

// ✅ CORRECT
<Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
```

### Issue 3: Navigate Not Working
**Symptom:** Token is stored but no navigation happens

**Fix:** Use `window.location.href` as a fallback:
```jsx
// In OAuth2Redirect.jsx
if (token) {
  localStorage.setItem('accessToken', token);
  
  // Force navigation using window.location as fallback
  window.location.href = '/';
}
```

### Issue 4: Multiple useEffect Calls
**Symptom:** Component re-renders multiple times

**Fix:** Add dependency check:
```jsx
const [hasNavigated, setHasNavigated] = useState(false);

useEffect(() => {
  if (hasNavigated) return; // Prevent multiple navigations
  
  const token = searchParams.get('token');
  if (token) {
    localStorage.setItem('accessToken', token);
    setHasNavigated(true);
    navigate('/', { replace: true });
  }
}, [searchParams, navigate, hasNavigated]);
```

---

## Quick Test

1. **Clear localStorage** (to start fresh):
   ```javascript
   localStorage.clear()
   ```

2. **Add console logs** to see what's happening:
   ```jsx
   console.log('Current URL:', window.location.href);
   console.log('Token from URL:', searchParams.get('token'));
   console.log('Token in localStorage:', localStorage.getItem('accessToken'));
   ```

3. **Try OAuth2 login again** and watch the console

---

## Debug Checklist

- [ ] Browser console shows "Token stored, redirecting to homepage..."
- [ ] localStorage has `accessToken` after login
- [ ] `/oauth2/redirect` route exists in App.jsx
- [ ] OAuth2Redirect component is NOT wrapped in ProtectedRoute
- [ ] HomePage route is `/` exactly
- [ ] No errors in browser console
- [ ] React Router v6 is installed (`react-router-dom@6.x`)

---

## If Still Stuck

Share with me:
1. Browser console output after clicking OAuth login
2. Your `App.jsx` routes configuration
3. Your current `OAuth2Redirect.jsx` code

I'll identify the exact issue!
