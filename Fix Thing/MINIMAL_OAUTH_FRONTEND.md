# Minimal OAuth2 Frontend Setup (No Axios Needed!)

This guide shows you how to implement OAuth2 login with homepage redirect using **only React Router** (no axios required).

---

## Prerequisites

```bash
npm install react-router-dom
```

That's it! No axios needed for OAuth2 login.

---

## File 1: OAuth2Redirect.jsx

**Purpose:** Handles the redirect from backend after successful OAuth2 login, extracts JWT token, and redirects to homepage.

**Location:** `src/pages/OAuth2Redirect.jsx`

```jsx
import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

export default function OAuth2Redirect() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const token = searchParams.get('token');
    const error = searchParams.get('error');

    if (token) {
      // Store JWT token
      localStorage.setItem('accessToken', token);
      
      // Extract and store user info from token (optional)
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        localStorage.setItem('userId', payload.userId);
        localStorage.setItem('role', payload.role);
      } catch (err) {
        console.error('Failed to parse token:', err);
      }
      
      // Redirect to homepage
      console.log('✅ OAuth2 login successful, redirecting to homepage...');
      navigate('/', { replace: true });
      
    } else if (error) {
      console.error('❌ OAuth2 login failed:', decodeURIComponent(error));
      alert('Login failed: ' + decodeURIComponent(error));
      navigate('/login', { replace: true });
    } else {
      console.error('❌ No token or error in URL');
      navigate('/login', { replace: true });
    }
  }, [searchParams, navigate]);

  return (
    <div style={styles.container}>
      <div style={styles.spinner}></div>
      <h2>Completing login...</h2>
      <p>Redirecting to homepage...</p>
    </div>
  );
}

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    background: '#f5f5f5',
  },
  spinner: {
    width: '40px',
    height: '40px',
    border: '4px solid #f3f3f3',
    borderTop: '4px solid #667eea',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite',
    marginBottom: '20px',
  },
};
```

**Add this CSS to your `index.css` or `App.css`:**
```css
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
```

---

## File 2: LoginPage.jsx (Minimal Version)

**Purpose:** Login page with Google and Facebook OAuth buttons.

**Location:** `src/pages/LoginPage.jsx`

```jsx
export default function LoginPage() {
  const BACKEND_URL = 'http://localhost:8081';

  // OAuth buttons use window.location.href - NO AXIOS NEEDED!
  const handleGoogleLogin = () => {
    window.location.href = `${BACKEND_URL}/oauth2/authorization/google`;
  };

  const handleFacebookLogin = () => {
    window.location.href = `${BACKEND_URL}/oauth2/authorization/facebook`;
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>🛒 B'Groceries</h1>
        <p style={styles.subtitle}>Sign in to your account</p>

        <button onClick={handleGoogleLogin} style={styles.googleBtn}>
          🔵 Sign in with Google
        </button>

        <button onClick={handleFacebookLogin} style={styles.facebookBtn}>
          📘 Sign in with Facebook
        </button>
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
  googleBtn: {
    width: '100%',
    padding: '14px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    background: 'white',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
    marginBottom: '12px',
    transition: 'all 0.2s',
  },
  facebookBtn: {
    width: '100%',
    padding: '14px',
    border: 'none',
    borderRadius: '8px',
    background: '#1877f2',
    color: 'white',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.2s',
  },
};
```

---

## File 3: App.jsx (Router Configuration)

**Purpose:** Configure React Router with OAuth2 redirect route.

**Location:** `src/App.jsx`

```jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import OAuth2Redirect from './pages/OAuth2Redirect';
import HomePage from './pages/HomePage'; // Your existing homepage

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
        <Route path="/" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />
        {/* Add other routes here */}
      </Routes>
    </BrowserRouter>
  );
}

// Protected Route Component - redirects to login if no token
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

## File 4: HomePage.jsx (Simple Example)

**Purpose:** Homepage that shows after successful OAuth2 login.

**Location:** `src/pages/HomePage.jsx`

```jsx
import { useNavigate } from 'react-router-dom';

export default function HomePage() {
  const navigate = useNavigate();
  const token = localStorage.getItem('accessToken');
  
  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('role');
    navigate('/login');
  };

  // Decode token to get user info
  let userName = 'User';
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      userName = payload.sub || 'User';
    } catch (err) {
      console.error('Failed to decode token:', err);
    }
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>🎉 Welcome to B'Groceries!</h1>
        <p style={styles.subtitle}>Hello, {userName}!</p>
        <p style={styles.text}>You successfully logged in with OAuth2.</p>
        <button onClick={handleLogout} style={styles.button}>
          Logout
        </button>
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
    maxWidth: '500px',
    width: '100%',
    boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
    textAlign: 'center',
  },
  title: {
    fontSize: '32px',
    fontWeight: '700',
    color: '#333',
    marginBottom: '16px',
  },
  subtitle: {
    fontSize: '20px',
    color: '#667eea',
    marginBottom: '12px',
  },
  text: {
    fontSize: '16px',
    color: '#666',
    marginBottom: '24px',
  },
  button: {
    padding: '12px 32px',
    border: 'none',
    borderRadius: '8px',
    background: '#ef4444',
    color: 'white',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.2s',
  },
};
```

---

## When You DO Need Axios

You only need axios for **regular API calls** (NOT OAuth2 login):

### Example: Fetch User Profile API (requires axios)

```jsx
import axios from 'axios';

const fetchUserProfile = async () => {
  const token = localStorage.getItem('accessToken');
  
  const response = await axios.get('http://localhost:8081/api/user/profile', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.data;
};
```

**But for OAuth2 login buttons, use `window.location.href` - NO axios needed!**

---

## Directory Structure

```
src/
├── App.jsx                     # Router configuration
├── pages/
│   ├── LoginPage.jsx          # OAuth login buttons
│   ├── OAuth2Redirect.jsx     # Handles OAuth callback & redirects to homepage
│   └── HomePage.jsx           # Homepage after login
└── index.css                  # Spinner animation CSS
```

---

## Testing Flow

1. **Start your frontend:**
   ```bash
   npm run dev
   ```
   Should run on `http://localhost:5173`

2. **Start your backend:**
   ```bash
   mvn spring-boot:run
   ```
   Should run on `http://localhost:8081`

3. **Test OAuth2 login:**
   - Go to: `http://localhost:5173/login`
   - Click "Sign in with Google" or "Sign in with Facebook"
   - Authorize on Google/Facebook
   - **You should be redirected to homepage:** `http://localhost:5173/`
   - ✅ **You're logged in!**

---

## Verify It Works

After successful login, check:

1. **Browser console** should show:
   ```
   ✅ OAuth2 login successful, redirecting to homepage...
   ```

2. **localStorage** should contain:
   ```javascript
   localStorage.getItem('accessToken') // JWT token
   localStorage.getItem('userId')      // Your user ID
   localStorage.getItem('role')        // USER or ADMIN
   ```

3. **URL** should be:
   ```
   http://localhost:5173/
   ```

---

## Summary

**For OAuth2 login and redirect to homepage, you DON'T need axios!**

✅ **What you need:**
- `react-router-dom` (for navigation and URL params)
- `window.location.href` (for OAuth button clicks)
- `useSearchParams()` (to extract token from URL)
- `navigate('/')` (to redirect to homepage)

❌ **What you DON'T need:**
- `axios` (not used in OAuth2 flow)

**Axios is only needed for:**
- Making authenticated API calls AFTER login
- Fetching data from protected endpoints
- CRUD operations with your backend API

But for the OAuth2 login flow itself, it's all browser redirects!
