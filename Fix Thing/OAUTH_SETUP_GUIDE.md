# OAuth2 Configuration Guide for B'Groceries

This guide explains how to fix the Google and Facebook OAuth errors you're experiencing.

## Current Errors

1. **Google**: `Error 400: redirect_uri_mismatch`
2. **Facebook**: `Failed to fetch user profile. Please try again.`

## Root Cause

Your OAuth2 providers (Google/Facebook) don't have the correct redirect URIs configured in their developer consoles.

## How OAuth2 Flow Works in Your App

1. User clicks "Sign in with Google/Facebook" on your frontend (`http://localhost:5173`)
2. Frontend redirects to backend OAuth endpoint: `http://localhost:8081/oauth2/authorization/{provider}`
3. Backend redirects to Google/Facebook for authentication
4. User authorizes on Google/Facebook
5. **Google/Facebook redirects back to backend**: `http://localhost:8081/login/oauth2/code/{provider}` ← **THIS MUST BE CONFIGURED**
6. Backend creates JWT token and redirects to frontend: `http://localhost:5173/oauth2/redirect?token={jwt}`
7. Frontend extracts token and redirects to homepage

---

## Fix 1: Configure Google OAuth2

### Step 1: Go to Google Cloud Console
1. Visit: https://console.cloud.google.com/apis/credentials
2. Find your OAuth 2.0 Client ID: `457341066065-0ja001e981hnhhe92uffiu7cbqpg6q1v`
3. Click on it to edit

### Step 2: Add Authorized Redirect URIs
Add these **exact** URIs (copy-paste to avoid typos):

**For development:**
```
http://localhost:8081/login/oauth2/code/google
```

**For production (when deployed):**
```
https://yourdomain.com/login/oauth2/code/google
```

### Step 3: Save Changes
Click **Save** at the bottom of the page.

### Step 4: Verify Configuration
Your Google Console should show:
- **Client ID**: `457341066065-0ja001e981hnhhe92uffiu7cbqpg6q1v`
- **Client Secret**: `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`
- **Authorized redirect URIs**: `http://localhost:8081/login/oauth2/code/google`

---

## Fix 2: Configure Facebook OAuth2

### Step 1: Go to Facebook Developers
1. Visit: https://developers.facebook.com/apps
2. Select your app (App ID: `1352113810463033`)
3. Go to **Settings** → **Basic** in the left sidebar

### Step 2: Add OAuth Redirect URIs
1. Scroll down to **Valid OAuth Redirect URIs**
2. Add these URIs:

**For development:**
```
http://localhost:8081/login/oauth2/code/facebook
```

**For production:**
```
https://yourdomain.com/login/oauth2/code/facebook
```

### Step 3: Configure Permissions
1. Go to **App Review** → **Permissions and Features**
2. Ensure these permissions are approved:
   - `email` (should be approved by default)
   - `public_profile` (should be approved by default)

### Step 4: Check App Mode
1. Go to **Settings** → **Basic**
2. At the top, check if your app is in **Development Mode** or **Live Mode**
   - **Development Mode**: Only works for test users/admins
   - **Live Mode**: Works for everyone (requires app review)

3. If in Development Mode, add your test email:
   - Go to **Roles** → **Test Users** or **Roles** → **Administrators**
   - Add `sokheng429@gmail.com` as a test user or admin

### Step 5: Verify Facebook Graph API Version
Your backend uses Graph API `v21.0`. Ensure your app supports this version:
- Go to **Settings** → **Advanced**
- Check **Upgrade API Version** - should be `v21.0` or higher

---

## Fix 3: Frontend OAuth2 Redirect Handler

Create this component in your React frontend to handle the redirect after successful OAuth login:

```jsx
// src/pages/OAuth2Redirect.jsx
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
      
      // Redirect to homepage
      navigate('/', { replace: true });
    } else if (error) {
      // Handle error
      console.error('OAuth2 login failed:', error);
      navigate('/login', { replace: true });
    }
  }, [searchParams, navigate]);

  return (
    <div style={{ textAlign: 'center', marginTop: '50px' }}>
      <h2>Completing login...</h2>
      <p>Please wait while we redirect you.</p>
    </div>
  );
}
```

Add this route to your React Router:
```jsx
// src/App.jsx or your router configuration
import OAuth2Redirect from './pages/OAuth2Redirect';

<Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
```

---

## Fix 4: Frontend OAuth Login Buttons

Your frontend should redirect to the backend OAuth endpoints:

```jsx
// LoginPage.jsx
const handleGoogleLogin = () => {
  window.location.href = 'http://localhost:8081/oauth2/authorization/google';
};

const handleFacebookLogin = () => {
  window.location.href = 'http://localhost:8081/oauth2/authorization/facebook';
};

// In your JSX:
<button onClick={handleGoogleLogin}>Sign in with Google</button>
<button onClick={handleFacebookLogin}>Sign in with Facebook</button>
```

---

## Testing the Flow

### Test Google OAuth:
1. Make sure Google Console has the correct redirect URI
2. Click "Sign in with Google" on your frontend
3. Authorize on Google
4. Should redirect back to homepage with JWT token stored

### Test Facebook OAuth:
1. Make sure Facebook app has the correct redirect URI
2. Ensure your email is added as test user/admin if in Development Mode
3. Click "Sign in with Facebook" on your frontend
4. Authorize on Facebook
5. Should redirect back to homepage with JWT token stored

---

## Common Issues & Solutions

### Google: "Access blocked: This app's request is invalid"
- **Solution**: Add exact redirect URI `http://localhost:8081/login/oauth2/code/google` to Google Console

### Facebook: "Failed to fetch user profile"
- **Solution 1**: Add redirect URI `http://localhost:8081/login/oauth2/code/facebook`
- **Solution 2**: Ensure your email is in the app's test users (if in Development Mode)
- **Solution 3**: Check that `email` and `public_profile` permissions are granted

### Facebook: "Can't Load URL: The domain of this URL isn't included in the app's domains"
- **Solution**: Go to **Settings** → **Basic** → **App Domains**
- Add: `localhost` (for development)

### Both: Redirect to wrong URL after login
- **Solution**: Check `app.oauth2.redirect-uri` in `application.yml` is set to `http://localhost:5173/oauth2/redirect`

---

## Production Deployment Checklist

When deploying to production:

1. **Google Console**:
   - Add production redirect URI: `https://yourdomain.com/login/oauth2/code/google`

2. **Facebook App**:
   - Add production redirect URI: `https://yourdomain.com/login/oauth2/code/facebook`
   - Switch app to **Live Mode**
   - Complete App Review if needed

3. **Backend application.yml**:
   - Set environment variable: `OAUTH2_REDIRECT_URI=https://your-frontend-domain.com/oauth2/redirect`

4. **Frontend**:
   - Update OAuth button URLs to production backend: `https://api.yourdomain.com/oauth2/authorization/{provider}`

---

## Need Help?

If you're still experiencing issues:
1. Check browser console for JavaScript errors
2. Check backend logs for detailed error messages
3. Verify all URLs match exactly (no trailing slashes, correct port numbers)
4. Test with incognito/private browsing to avoid cached credentials
