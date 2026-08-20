# React OAuth2 Components for B'Groceries Frontend

This document contains the React components you need to implement OAuth2 login with Google and Facebook, including automatic redirect to the homepage after successful authentication.

---

## 1. OAuth2 Redirect Handler Component

Create this component to handle the OAuth2 callback from the backend. It extracts the JWT token from the URL and redirects to the homepage.

**File: `src/pages/OAuth2Redirect.jsx`**

```jsx
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

/**
 * OAuth2 Redirect Handler
 * 
 * This page is where users land after successful OAuth2 authentication.
 * The backend redirects here with the JWT token as a query parameter.
 * 
 * Flow:
 * 1. User clicks "Sign in with Google/Facebook"
 * 2. Backend handles OAuth2 flow with provider
 * 3. Backend redirects to: http://localhost:5173/oauth2/redirect?token=JWT_TOKEN
 * 4. This component extracts the token, stores it, and redirects to homepage
 */
export default function OAuth2Redirect() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('processing');

  useEffect(() => {
    const token = searchParams.get('token');
    const error = searchParams.get('error');

    if (token) {
      try {
        // Store JWT token in localStorage
        localStorage.setItem('accessToken', token);
        
        // Optionally decode and store user info
        const payload = JSON.parse(atob(token.split('.')[1]));
        localStorage.setItem('userId', payload.userId);
        localStorage.setItem('role', payload.role);
        
        setStatus('success');
        
        // Redirect to homepage after a brief delay
        setTimeout(() => {
          navigate('/', { replace: true });
        }, 500);
        
      } catch (err) {
        console.error('Failed to process token:', err);
        setStatus('error');
        setTimeout(() => {
          navigate('/login', { replace: true });
        }, 2000);
      }
    } else if (error) {
      console.error('OAuth2 login failed:', error);
      setStatus('error');
      
      setTimeout(() => {
        navigate('/login', { 
          replace: true,
          state: { error: decodeURIComponent(error) }
        });
      }, 2000);
    } else {
      // No token or error - something went wrong
      setStatus('error');
      setTimeout(() => {
        navigate('/login', { replace: true });
      }, 2000);
    }
  }, [searchParams, navigate]);

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        {status === 'processing' && (
          <>
            <div style={styles.spinner}></div>
            <h2 style={styles.title}>Completing your login...</h2>
            <p style={styles.subtitle}>Please wait a moment</p>
          </>
        )}
        
        {status === 'success' && (
          <>
            <div style={styles.successIcon}>✓</div>
            <h2 style={styles.title}>Login successful!</h2>
            <p style={styles.subtitle}>Redirecting to homepage...</p>
          </>
        )}
        
        {status === 'error' && (
          <>
            <div style={styles.errorIcon}>✗</div>
            <h2 style={styles.title}>Login failed</h2>
            <p style={styles.subtitle}>Redirecting back to login...</p>
          </>
        )}
      </div>
    </div>
  );
}

// Inline styles for the loading page
const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
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
  successIcon: {
    fontSize: '60px',
    color: '#22c55e',
    marginBottom: '20px',
  },
  errorIcon: {
    fontSize: '60px',
    color: '#ef4444',
    marginBottom: '20px',
  },
  title: {
    fontSize: '24px',
    fontWeight: '600',
    color: '#333',
    marginBottom: '10px',
  },
  subtitle: {
    fontSize: '16px',
    color: '#666',
  },
};

// Add this CSS to your global styles or add a <style> tag in index.html
// @keyframes spin {
//   0% { transform: rotate(0deg); }
//   100% { transform: rotate(360deg); }
// }
```

---

## 2. Login Page with OAuth Buttons

Update your login page to include Google and Facebook OAuth buttons.

**File: `src/pages/LoginPage.jsx`**

```jsx
import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const BACKEND_URL = 'http://localhost:8081';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [formData, setFormData] = useState({
    identifier: '',
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(location.state?.error || '');

  // Handle regular username/password login
  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch(`${BACKEND_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      const result = await response.json();

      if (result.success) {
        // Store JWT token
        localStorage.setItem('accessToken', result.data.token);
        localStorage.setItem('userId', result.data.user.id);
        localStorage.setItem('role', result.data.user.role);
        
        // Redirect to homepage
        navigate('/', { replace: true });
      } else {
        setError(result.message || 'Login failed');
      }
    } catch (err) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Handle Google OAuth login
  const handleGoogleLogin = () => {
    // Redirect to backend OAuth2 endpoint
    window.location.href = `${BACKEND_URL}/oauth2/authorization/google`;
  };

  // Handle Facebook OAuth login
  const handleFacebookLogin = () => {
    // Redirect to backend OAuth2 endpoint
    window.location.href = `${BACKEND_URL}/oauth2/authorization/facebook`;
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>🛒 B'Groceries</h1>
        <p style={styles.subtitle}>Sign in to your account</p>

        {error && (
          <div style={styles.errorBanner}>
            {error}
          </div>
        )}

        {/* OAuth Buttons */}
        <button 
          onClick={handleGoogleLogin} 
          style={styles.googleBtn}
          type="button"
        >
          <GoogleIcon />
          <span>Sign in with Google</span>
        </button>

        <button 
          onClick={handleFacebookLogin} 
          style={styles.facebookBtn}
          type="button"
        >
          <FacebookIcon />
          <span>Sign in with Facebook</span>
        </button>

        <div style={styles.divider}>
          <span>or</span>
        </div>

        {/* Username/Password Form */}
        <form onSubmit={handleLogin}>
          <input
            type="text"
            placeholder="Username, email, or phone"
            value={formData.identifier}
            onChange={(e) => setFormData({ ...formData, identifier: e.target.value })}
            style={styles.input}
            required
          />
          
          <input
            type="password"
            placeholder="Password"
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            style={styles.input}
            required
          />

          <button 
            type="submit" 
            style={styles.submitBtn}
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p style={styles.footer}>
          Don't have an account? <a href="/register" style={styles.link}>Sign up</a>
        </p>
      </div>
    </div>
  );
}

// SVG Icons
const GoogleIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24">
    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
  </svg>
);

const FacebookIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="white">
    <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
  </svg>
);

// Styles
const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    padding: '20px',
  },
  card: {
    background: 'white',
    borderRadius: '16px',
    padding: '40px',
    maxWidth: '400px',
    width: '100%',
    boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
  },
  title: {
    fontSize: '28px',
    fontWeight: '700',
    color: '#333',
    marginBottom: '8px',
    textAlign: 'center',
  },
  subtitle: {
    textAlign: 'center',
    color: '#666',
    marginBottom: '24px',
  },
  errorBanner: {
    background: '#fee',
    color: '#c33',
    padding: '12px',
    borderRadius: '8px',
    marginBottom: '16px',
    fontSize: '14px',
    textAlign: 'center',
  },
  googleBtn: {
    width: '100%',
    padding: '12px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    background: 'white',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '12px',
    marginBottom: '12px',
    transition: 'all 0.2s',
  },
  facebookBtn: {
    width: '100%',
    padding: '12px',
    border: 'none',
    borderRadius: '8px',
    background: '#1877f2',
    color: 'white',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '12px',
    marginBottom: '20px',
    transition: 'all 0.2s',
  },
  divider: {
    textAlign: 'center',
    margin: '24px 0',
    position: 'relative',
    color: '#888',
    fontSize: '14px',
  },
  input: {
    width: '100%',
    padding: '12px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    fontSize: '16px',
    marginBottom: '12px',
    boxSizing: 'border-box',
  },
  submitBtn: {
    width: '100%',
    padding: '12px',
    border: 'none',
    borderRadius: '8px',
    background: '#667eea',
    color: 'white',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.2s',
  },
  footer: {
    textAlign: 'center',
    marginTop: '20px',
    fontSize: '14px',
    color: '#666',
  },
  link: {
    color: '#667eea',
    textDecoration: 'none',
    fontWeight: '600',
  },
};
```

---

## 3. Router Configuration

Update your React Router to include the OAuth2 redirect route.

**File: `src/App.jsx` (or wherever you configure routes)**

```jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import OAuth2Redirect from './pages/OAuth2Redirect';
import HomePage from './pages/HomePage';
// ... other imports

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
        <Route path="/" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />
        {/* ... other routes */}
      </Routes>
    </BrowserRouter>
  );
}

// Protected Route Component
function ProtectedRoute({ children }) {
  const token = localStorage.getItem('accessToken');
  
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  
  return children;
}

export default App;
```

---

## 4. Auth Utility Functions (Optional)

Create helper functions to manage authentication state.

**File: `src/utils/auth.js`**

```javascript
const BACKEND_URL = 'http://localhost:8081';

/**
 * Check if user is authenticated
 */
export function isAuthenticated() {
  const token = localStorage.getItem('accessToken');
  if (!token) return false;
  
  try {
    // Check if token is expired
    const payload = JSON.parse(atob(token.split('.')[1]));
    const expiresAt = payload.exp * 1000; // Convert to milliseconds
    return Date.now() < expiresAt;
  } catch {
    return false;
  }
}

/**
 * Get current user info from token
 */
export function getCurrentUser() {
  const token = localStorage.getItem('accessToken');
  if (!token) return null;
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return {
      userId: payload.userId,
      role: payload.role,
      subject: payload.sub,
    };
  } catch {
    return null;
  }
}

/**
 * Logout user
 */
export function logout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('userId');
  localStorage.removeItem('role');
  window.location.href = '/login';
}

/**
 * Make authenticated API request
 */
export async function fetchWithAuth(endpoint, options = {}) {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch(`${BACKEND_URL}${endpoint}`, {
    ...options,
    headers: {
      ...options.headers,
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  });
  
  // If 401 Unauthorized, logout
  if (response.status === 401) {
    logout();
    throw new Error('Session expired');
  }
  
  return response;
}
```

---

## 5. Environment Configuration

Create an environment file for the backend URL.

**File: `.env` (in your React project root)**

```env
VITE_BACKEND_URL=http://localhost:8081
VITE_OAUTH2_REDIRECT_URI=http://localhost:5173/oauth2/redirect
```

Then update your code to use environment variables:

```javascript
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
```

---

## 6. Testing Your OAuth2 Flow

### Quick Test Steps:

1. **Start your backend**: `mvn spring-boot:run` (should be running on `http://localhost:8081`)

2. **Start your frontend**: `npm run dev` (should be running on `http://localhost:5173`)

3. **Test Google Login**:
   - Go to `http://localhost:5173/login`
   - Click "Sign in with Google"
   - You should be redirected to Google's login page
   - After authorization, you'll be redirected back to `http://localhost:5173/oauth2/redirect?token=JWT_TOKEN`
   - The OAuth2Redirect component will extract the token and redirect to homepage

4. **Test Facebook Login**:
   - Same flow but with Facebook button

### Troubleshooting:

- **"redirect_uri_mismatch"**: Make sure you added `http://localhost:8081/login/oauth2/code/google` to Google Console
- **"Failed to fetch user profile"**: Make sure you added `http://localhost:8081/login/oauth2/code/facebook` to Facebook App and your email is in test users (if in Development Mode)
- **Token not received**: Check browser console for errors, and backend logs for OAuth2 flow errors
- **CORS errors**: Make sure your backend CORS config allows `http://localhost:5173`

---

## 7. Backend Test Page

You can also test the OAuth2 flow without React by visiting:

```
http://localhost:8081/oauth-test.html
```

This is a standalone HTML page that tests the OAuth2 flow and displays the JWT token.

---

## Summary

**What happens when a user logs in with Google/Facebook:**

1. User clicks "Sign in with Google" button
2. Frontend redirects to: `http://localhost:8081/oauth2/authorization/google`
3. Backend redirects to Google for authentication
4. User authorizes on Google
5. Google redirects back to: `http://localhost:8081/login/oauth2/code/google`
6. Backend creates JWT token and redirects to: `http://localhost:5173/oauth2/redirect?token=JWT_TOKEN`
7. OAuth2Redirect component extracts token, stores it in localStorage
8. Component redirects to homepage: `http://localhost:5173/`
9. User is now logged in!

**After successful login, the token is stored and the user is automatically redirected to the homepage (`/`).**
