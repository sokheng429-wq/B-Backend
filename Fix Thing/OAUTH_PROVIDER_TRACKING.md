# OAuth Login Provider Tracking - Feature Documentation

## Overview

Your B'Groceries backend now tracks which OAuth provider (Google, Facebook, Telegram) each user used to login. This information is stored in the database and returned in API responses.

---

## 🎯 What's Been Added

### 1. **Database Field**
- New column: `login_provider` in `users` table
- Stores: `"google"`, `"facebook"`, `"telegram"`, or `null` (for password login)
- Updated automatically on every OAuth login

### 2. **User Response**
- `/api/users/me` now includes `loginProvider` field
- Shows which OAuth provider was used for the most recent login

### 3. **Automatic Tracking**
- When a user logs in with Google → `loginProvider = "google"`
- When a user logs in with Facebook → `loginProvider = "facebook"`
- When a user logs in with Telegram → `loginProvider = "telegram"`
- When a user logs in with password → `loginProvider = null`

---

## 📊 Database Schema

### Updated `users` Table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100) UNIQUE,
    telegram VARCHAR(100) UNIQUE,
    facebook VARCHAR(100) UNIQUE,
    
    -- OAuth provider IDs (for account linking)
    google_id VARCHAR(100) UNIQUE,
    facebook_id VARCHAR(100) UNIQUE,
    telegram_id VARCHAR(100) UNIQUE,
    
    -- NEW: Track which provider was used for login
    login_provider VARCHAR(20),  -- 'google', 'facebook', 'telegram', or NULL
    
    phone_number VARCHAR(20) UNIQUE,
    date_of_birth DATE,
    gender VARCHAR(20),
    nationality VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

**Note:** The `login_provider` column will be automatically created when you run the application (JPA auto-update).

---

## 🔍 How to View OAuth Provider Information

### Option 1: API Response

When a user logs in with Google/Facebook and calls `/api/users/me`:

```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": 23,
    "fullName": "Heng Sok",
    "username": "hengsok",
    "email": "b.groceriesdev@gmail.com",
    "telegram": null,
    "facebook": null,
    "phoneNumber": null,
    "role": "ADMIN",
    "dateOfBirth": null,
    "gender": null,
    "nationality": null,
    "loginProvider": "google"  ← Shows user logged in with Google
  }
}
```

### Option 2: Database Query

Connect to your PostgreSQL database and run:

```sql
-- See all users and their login providers
SELECT 
    id,
    full_name,
    username,
    email,
    login_provider,
    google_id,
    facebook_id,
    telegram_id,
    created_at
FROM users
ORDER BY created_at DESC;
```

**Example output:**
```
 id |  full_name  | username |         email           | login_provider | google_id | facebook_id | telegram_id
----+-------------+----------+-------------------------+----------------+-----------+-------------+-------------
 23 | Heng Sok    | hengsok  | b.groceriesdev@gmail.com| google         | 114466... | null        | null
 22 | ធឿន សុខហេង  | user     | you2melove4@gmail.com   | google         | 110533... | null        | null
 21 | Ling Fu     | lingfu   | sokheng429@gmail.com    | facebook       | null      | 36019...    | null
  2 | Admin       | admin    | admin@bgroceries.com    | null           | null      | null        | null
```

### Option 3: Backend Logs

When users login with OAuth, check backend logs:

```
=== OAuth2 User Load Started ===
Provider: google
Received OAuth2 attributes: [sub, name, email, ...]
User loaded successfully: hengsok (ID: 23)
```

---

## 🎨 Frontend Display Examples

### Example 1: Show Login Method Badge

```jsx
function UserProfile({ user }) {
  const getProviderBadge = (provider) => {
    if (!provider) return <span className="badge">Password</span>;
    
    const badges = {
      google: { icon: '🔵', text: 'Google', color: '#4285F4' },
      facebook: { icon: '📘', text: 'Facebook', color: '#1877f2' },
      telegram: { icon: '✈️', text: 'Telegram', color: '#0088cc' }
    };
    
    const badge = badges[provider];
    return (
      <span style={{ 
        background: badge.color, 
        color: 'white',
        padding: '4px 12px',
        borderRadius: '12px',
        fontSize: '12px'
      }}>
        {badge.icon} {badge.text}
      </span>
    );
  };

  return (
    <div>
      <h2>{user.fullName}</h2>
      <p>Login method: {getProviderBadge(user.loginProvider)}</p>
    </div>
  );
}
```

### Example 2: Show Provider Icon in Header

```jsx
function UserAvatar({ user }) {
  const providerIcons = {
    google: '🔵',
    facebook: '📘',
    telegram: '✈️'
  };
  
  return (
    <div className="user-avatar">
      <img src={user.avatarUrl} alt={user.fullName} />
      {user.loginProvider && (
        <span className="provider-badge">
          {providerIcons[user.loginProvider]}
        </span>
      )}
    </div>
  );
}
```

### Example 3: Admin Dashboard - User List with Providers

```jsx
function UserList({ users }) {
  return (
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Login Method</th>
          <th>Joined</th>
        </tr>
      </thead>
      <tbody>
        {users.map(user => (
          <tr key={user.id}>
            <td>{user.fullName}</td>
            <td>{user.email}</td>
            <td>
              {user.loginProvider ? (
                <span className={`badge ${user.loginProvider}`}>
                  {user.loginProvider.toUpperCase()}
                </span>
              ) : (
                <span className="badge password">PASSWORD</span>
              )}
            </td>
            <td>{new Date(user.createdAt).toLocaleDateString()}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```

---

## 🔐 Security & Privacy Considerations

### What's Stored:
- ✅ **Provider name**: `"google"`, `"facebook"`, `"telegram"`
- ✅ **Provider user ID**: Stable identifier from the provider (e.g., `google_id`)
- ✅ **Email/username**: Retrieved from provider

### What's NOT Stored:
- ❌ OAuth access tokens (not needed after login)
- ❌ OAuth refresh tokens
- ❌ Provider passwords (OAuth providers handle authentication)

### Privacy Notes:
- The `loginProvider` field shows **which method** was used, not sensitive credentials
- Users can see their own `loginProvider` in their profile
- Admins can see all users' `loginProvider` values for support purposes

---

## 📋 Use Cases

### 1. **User Support**
When a user forgets their password:
```sql
-- Check if user has password login or social login only
SELECT username, email, login_provider, password_hash IS NOT NULL as has_password
FROM users WHERE email = 'user@example.com';
```

**If `login_provider` is `"google"`:**
→ Tell user: "You signed up with Google, please use 'Sign in with Google' button"

**If `login_provider` is `null` and `has_password` is `true`:**
→ User can use "Forgot Password" feature

### 2. **Analytics**
Track which OAuth providers are most popular:
```sql
-- Count users by login method
SELECT 
    COALESCE(login_provider, 'password') as method,
    COUNT(*) as user_count
FROM users
GROUP BY login_provider
ORDER BY user_count DESC;
```

### 3. **Account Linking**
Allow users to link multiple providers to one account:
```sql
-- User has both Google and Facebook linked
SELECT 
    username,
    email,
    google_id IS NOT NULL as has_google,
    facebook_id IS NOT NULL as has_facebook,
    login_provider as last_used_method
FROM users
WHERE id = 23;
```

---

## 🧪 Testing

### Test 1: Login with Google
```bash
# 1. Login with Google in your frontend
# 2. Check the user data:
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8081/api/users/me
```

**Expected response:**
```json
{
  "loginProvider": "google"
}
```

### Test 2: Login with Facebook
```bash
# 1. Login with Facebook in your frontend
# 2. Check the user data (should update to "facebook")
```

### Test 3: Password Login
```bash
# 1. Login with username/password
# 2. Check the user data:
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8081/api/users/me
```

**Expected response:**
```json
{
  "loginProvider": null
}
```

---

## 🔄 How It Updates

The `loginProvider` field is updated **every time the user logs in**:

1. **User logs in with Google** → `loginProvider = "google"`
2. **Same user logs in with Facebook** → `loginProvider = "facebook"` (updates to latest)
3. **Same user logs in with password** → `loginProvider = null`

This tracks the **most recent login method**, not the original signup method.

---

## 📊 Sample Database Data

After users login with different providers:

```sql
SELECT id, username, email, login_provider, google_id, facebook_id 
FROM users;
```

```
 id | username |         email           | login_provider |  google_id   | facebook_id
----+----------+-------------------------+----------------+--------------+-------------
 23 | hengsok  | b.groceriesdev@gmail.com| google         | 114466010... | null
 22 | user     | you2melove4@gmail.com   | google         | 110533588... | null
 21 | lingfu   | sokheng429@gmail.com    | facebook       | null         | 3601968...
  2 | admin    | admin@bgroceries.com    | null           | null         | null
```

**Key observations:**
- Users 23 & 22: Logged in with Google → `loginProvider = "google"`, have `google_id`
- User 21: Logged in with Facebook → `loginProvider = "facebook"`, has `facebook_id`
- User 2: Admin created with password → `loginProvider = null`

---

## 🚀 Next Steps

1. **Display provider info in your frontend**
   - Show badge/icon for OAuth users
   - Help users understand which login method they used

2. **Add account settings page**
   - Allow users to see which providers are linked
   - Option to link/unlink OAuth providers

3. **Improve user support**
   - When user forgets password, check `loginProvider` first
   - Guide them to correct login method

---

## 💡 Pro Tips

1. **Check login provider before password reset:**
   ```javascript
   if (user.loginProvider !== null) {
     alert(`You signed up with ${user.loginProvider}. Please use "Sign in with ${user.loginProvider}" button.`);
   }
   ```

2. **Show appropriate login options:**
   ```javascript
   // On login page, highlight the provider they used last time
   if (lastLoginProvider === 'google') {
     // Make Google button more prominent
   }
   ```

3. **Account linking:**
   ```javascript
   // Show which providers are already linked
   const linkedProviders = {
     google: user.googleId !== null,
     facebook: user.facebookId !== null,
     telegram: user.telegramId !== null
   };
   ```

---

## Summary

✅ **Database field added:** `login_provider` column
✅ **Auto-tracking:** Updates on every OAuth login
✅ **API response:** Included in `/api/users/me`
✅ **Supported providers:** Google, Facebook, Telegram
✅ **Password login:** Shows as `null`

Your backend now tracks which OAuth provider each user uses for login, making it easy to provide better user support and analytics! 🎉
