# OAuth2 "Failed to Fetch User Profile" Error - Diagnostic Guide

**Error**: "Failed to fetch user profile. Please try again."

**When it occurs**: After successfully clicking "Continue" on Google/Facebook authorization page

---

## 🔍 Step-by-Step Diagnosis

### Step 1: Check Backend Logs

The backend is now running with enhanced error logging. After you see the error, check the logs to see the exact exception.

**How to check logs:**

**Option A - Real-time monitoring:**
```bash
# In a separate terminal, monitor the logs in real-time
tail -f D:\1.B.Groceries\Backend\B-backend\target\spring-boot.log
```

**Option B - Check recent errors:**
After the error occurs, run:
```bash
cd D:\1.B.Groceries\Backend\B-backend
# Look for the error in console output
```

**What to look for in logs:**
```
=== OAuth2 User Load Started ===
Provider: google (or facebook)
Received OAuth2 attributes: [...]
OAuth2 Login Failed: [ERROR MESSAGE HERE]
Exception Type: [EXCEPTION CLASS]
Root Cause: [ROOT CAUSE MESSAGE]
```

---

### Step 2: Test OAuth2 Connectivity

Visit these diagnostic endpoints to check if your backend can reach Google/Facebook:

1. **Check OAuth2 credentials configuration:**
   ```
   http://localhost:8081/api/oauth2/diagnostic/credentials
   ```
   Should show:
   ```json
   {
     "google": {
       "clientIdConfigured": true,
       "clientSecretConfigured": true,
       "clientIdLength": 72,
       "clientSecretLength": 35
     },
     "facebook": {
       "clientIdConfigured": true,
       "clientSecretConfigured": true,
       "clientIdLength": 16,
       "clientSecretLength": 32
     }
   }
   ```

2. **Test Google connectivity:**
   ```
   http://localhost:8081/api/oauth2/diagnostic/test-google
   ```
   Should return: `{"status":"success","reachable":true}`

3. **Test Facebook connectivity:**
   ```
   http://localhost:8081/api/oauth2/diagnostic/test-facebook
   ```
   Should return: `{"status":"success","reachable":true}`

---

## 🐛 Common Causes & Solutions

### Cause 1: Invalid OAuth2 Client Credentials ⚠️

**Symptoms:**
- Logs show: `401 Unauthorized` or `invalid_client`
- You successfully authorize on Google/Facebook, but backend can't exchange the code for tokens

**Solution:**
1. Verify your Google Cloud Console credentials:
   - Go to https://console.cloud.google.com/apis/credentials
   - Check Client ID matches: `457341066065-0ja001e981hnhhe92uffiu7cbqpg6q1v.apps.googleusercontent.com`
   - Check Client Secret matches what's in `application.yml`

2. Verify Facebook App credentials:
   - Go to https://developers.facebook.com/apps
   - Settings → Basic
   - Check App ID matches: `1352113810463033`
   - Check App Secret matches: `e64907e71514d2f8b2f5af4b3c060eda`

---

### Cause 2: Missing User Info Endpoint Permissions 🔐

**Symptoms:**
- Logs show: `insufficient_scope` or `access_denied`
- OAuth2 token exchange succeeds, but fetching user profile fails

**Solution for Google:**
1. Go to https://console.cloud.google.com/apis/credentials
2. Click on your OAuth 2.0 Client ID
3. Ensure **Authorized scopes** include:
   - `email`
   - `profile`
   - `openid`

**Solution for Facebook:**
1. Go to https://developers.facebook.com/apps → Your App
2. **App Review** → **Permissions and Features**
3. Ensure these permissions are approved:
   - `email` (should be default)
   - `public_profile` (should be default)

4. Check **Use Cases**:
   - Facebook now requires apps to declare "Use Cases"
   - Go to **Use Cases** in left sidebar
   - Add "Authentication and account creation" use case
   - Request `email` and `public_profile` permissions for this use case

---

### Cause 3: Facebook App in Development Mode 🚧

**Symptoms:**
- Google OAuth works fine
- Facebook OAuth shows "Failed to fetch user profile"
- Logs might show: `OAuthException` or user not authorized

**Solution:**
1. Go to https://developers.facebook.com/apps → Your App
2. Check the toggle at the top: **Development** vs **Live**

**If in Development Mode:**
- Only works for users added as **Test Users**, **Developers**, or **Administrators**
- **Add your test account:**
  1. Go to **Roles** → **Test Users** (or **Administrators**)
  2. Add `sokheng429@gmail.com`
  3. Try logging in again

**To go Live:**
- Complete Facebook's App Review process
- This takes several days and requires screenshots, privacy policy, etc.

---

### Cause 4: Redirect URI Mismatch (Backend) 🔄

**Symptoms:**
- Error occurs even after successfully authorizing on Google/Facebook
- Logs show: `redirect_uri_mismatch` in OAuth2 token exchange

**Solution:**
Your `application.yml` has:
```yaml
redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
```

The `{baseUrl}` should resolve to `http://localhost:8081`.

**Verify the actual redirect URI:**
```bash
curl -i http://localhost:8081/oauth2/authorization/google | grep Location
```

Should show:
```
redirect_uri=http://localhost:8081/login/oauth2/code/google
```

**If it shows a different URL (e.g., `http://127.0.0.1` or `http://[your-ip]`):**
- Add that exact URL to your Google/Facebook console as well

---

### Cause 5: Network/Firewall Issues 🌐

**Symptoms:**
- Diagnostic endpoints show `reachable: false`
- Logs show: `Connection timeout` or `UnknownHostException`

**Solution:**
1. Check if your firewall is blocking outbound HTTPS requests
2. Try from command line:
   ```bash
   curl https://accounts.google.com/.well-known/openid-configuration
   curl https://graph.facebook.com/v21.0/
   ```
3. If these fail, your network is blocking OAuth2 providers
4. Configure proxy if needed in `application.yml`:
   ```yaml
   spring:
     http:
       client:
         proxy:
           host: your-proxy-host
           port: 8080
   ```

---

### Cause 6: Database Connection Issue 💾

**Symptoms:**
- Logs show: `JdbcException` or `PSQLException`
- OAuth2 user profile fetch succeeds, but saving user to database fails

**Solution:**
Check your PostgreSQL database connection:
```bash
# Test database connectivity
psql -h localhost -U postgres -d bgroceries -c "SELECT 1;"
```

If connection fails:
- Verify `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` environment variables
- Check if PostgreSQL is running
- Verify database `bgroceries` exists

---

### Cause 7: PasswordEncoder Bean Missing 🔧

**Symptoms:**
- Logs show: `NoSuchBeanDefinitionException: No qualifying bean of type 'PasswordEncoder'`

**Solution:**
Check if `PasswordEncoderConfig` exists:
```bash
ls src/main/java/com/bgroceries/backend/config/PasswordEncoderConfig.java
```

If missing, create it:
```java
package com.bgroceries.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 📝 Next Steps

1. **Try to log in with Google/Facebook again**
2. **Immediately check the backend console/logs** for the detailed error message
3. **Share the error message** from the logs so I can provide the exact solution

The enhanced logging I added will show:
- Which provider (Google/Facebook)
- What OAuth2 attributes were received
- The exact exception and root cause

Once you provide the error from the logs, I can give you the precise fix!

---

## 🧪 Quick Test

Run these commands to verify your setup:

```bash
# 1. Check server is running
curl http://localhost:8081/api/oauth2/config

# 2. Check OAuth2 credentials are configured
curl http://localhost:8081/api/oauth2/diagnostic/credentials

# 3. Test Google connectivity
curl http://localhost:8081/api/oauth2/diagnostic/test-google

# 4. Test Facebook connectivity
curl http://localhost:8081/api/oauth2/diagnostic/test-facebook

# 5. Try OAuth2 flow
# Open in browser:
http://localhost:8081/oauth-test.html
```

All checks should pass before attempting OAuth2 login.
