# Complete Registration Form with Additional Fields

## ✅ Backend Updated

Your backend now accepts these fields in registration:

| Field | Required | Description |
|-------|----------|-------------|
| `username` | ✅ Yes | 2-50 characters |
| `fullName` | ✅ Yes | 2-100 characters |
| `email` | ✅ Yes | Valid email format |
| `phoneNumber` | ✅ Yes | Cambodian format (012345678 or +855...) |
| `password` | ✅ Yes | Minimum 6 characters |
| `confirmPassword` | ✅ Yes | Must match password |
| `dateOfBirth` | ⭕ Optional | Date format (YYYY-MM-DD) |
| `gender` | ⭕ Optional | "Male", "Female", "Other" |
| `nationality` | ⭕ Optional | Country name |

---

## 🎨 React Registration Component

### File: `src/pages/RegisterPage.jsx`

```jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const BACKEND_URL = 'http://localhost:8081';

// List of countries for nationality dropdown
const COUNTRIES = [
  'Cambodia', 'Thailand', 'Vietnam', 'Laos', 'Myanmar',
  'Singapore', 'Malaysia', 'Indonesia', 'Philippines',
  'United States', 'United Kingdom', 'China', 'Japan',
  'South Korea', 'India', 'Australia', 'Canada', 'Other'
];

export default function RegisterPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    username: '',
    fullName: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
    dateOfBirth: '',
    gender: '',
    nationality: ''
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    // Basic validation
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(`${BACKEND_URL}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
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
        setError(result.message || 'Registration failed');
      }
    } catch (err) {
      setError('Network error. Please try again.');
      console.error('Registration error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>🛒 Create Account</h1>
        <p style={styles.subtitle}>Join B'Groceries today</p>

        {error && (
          <div style={styles.errorBanner}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Username */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Username *</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              style={styles.input}
              placeholder="Choose a username"
              required
              minLength={2}
              maxLength={50}
            />
          </div>

          {/* Full Name */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Full Name *</label>
            <input
              type="text"
              name="fullName"
              value={formData.fullName}
              onChange={handleChange}
              style={styles.input}
              placeholder="Enter your full name"
              required
              minLength={2}
              maxLength={100}
            />
          </div>

          {/* Email */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Email *</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              style={styles.input}
              placeholder="your.email@example.com"
              required
            />
          </div>

          {/* Phone Number */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Phone Number *</label>
            <input
              type="tel"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              style={styles.input}
              placeholder="012345678 or +855 12 345 678"
              required
              pattern="^(0|\+855)[0-9]{8,9}$"
            />
            <small style={styles.hint}>Format: 012345678 or +855 12 345 678</small>
          </div>

          {/* Date of Birth */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Date of Birth</label>
            <input
              type="date"
              name="dateOfBirth"
              value={formData.dateOfBirth}
              onChange={handleChange}
              style={styles.input}
              max={new Date().toISOString().split('T')[0]} // Can't be in the future
            />
          </div>

          {/* Gender */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Gender</label>
            <select
              name="gender"
              value={formData.gender}
              onChange={handleChange}
              style={styles.select}
            >
              <option value="">Select gender (optional)</option>
              <option value="Male">Male</option>
              <option value="Female">Female</option>
              <option value="Other">Other</option>
              <option value="Prefer not to say">Prefer not to say</option>
            </select>
          </div>

          {/* Nationality */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Nationality</label>
            <select
              name="nationality"
              value={formData.nationality}
              onChange={handleChange}
              style={styles.select}
            >
              <option value="">Select nationality (optional)</option>
              {COUNTRIES.map(country => (
                <option key={country} value={country}>{country}</option>
              ))}
            </select>
          </div>

          {/* Password */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Password *</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              style={styles.input}
              placeholder="At least 6 characters"
              required
              minLength={6}
            />
          </div>

          {/* Confirm Password */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Confirm Password *</label>
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              style={styles.input}
              placeholder="Re-enter your password"
              required
              minLength={6}
            />
          </div>

          {/* Submit Button */}
          <button 
            type="submit" 
            style={{
              ...styles.submitBtn,
              opacity: loading ? 0.6 : 1,
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
            disabled={loading}
          >
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <div style={styles.divider}>
          <span>or</span>
        </div>

        {/* OAuth Buttons */}
        <button 
          onClick={() => window.location.href = `${BACKEND_URL}/oauth2/authorization/google`}
          style={styles.googleBtn}
          type="button"
        >
          🔵 Sign up with Google
        </button>

        <button 
          onClick={() => window.location.href = `${BACKEND_URL}/oauth2/authorization/facebook`}
          style={styles.facebookBtn}
          type="button"
        >
          📘 Sign up with Facebook
        </button>

        <p style={styles.footer}>
          Already have an account? <a href="/login" style={styles.link}>Sign in</a>
        </p>
      </div>
    </div>
  );
}

const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    padding: '40px 20px',
  },
  card: {
    background: 'white',
    borderRadius: '16px',
    padding: '40px',
    maxWidth: '500px',
    width: '100%',
    boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
    maxHeight: '90vh',
    overflowY: 'auto',
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
  formGroup: {
    marginBottom: '16px',
  },
  label: {
    display: 'block',
    fontSize: '14px',
    fontWeight: '600',
    color: '#333',
    marginBottom: '6px',
  },
  input: {
    width: '100%',
    padding: '12px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    fontSize: '16px',
    boxSizing: 'border-box',
    transition: 'border-color 0.2s',
  },
  select: {
    width: '100%',
    padding: '12px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    fontSize: '16px',
    boxSizing: 'border-box',
    background: 'white',
    cursor: 'pointer',
  },
  hint: {
    display: 'block',
    fontSize: '12px',
    color: '#888',
    marginTop: '4px',
  },
  submitBtn: {
    width: '100%',
    padding: '14px',
    border: 'none',
    borderRadius: '8px',
    background: '#667eea',
    color: 'white',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
    marginTop: '8px',
    transition: 'all 0.2s',
  },
  divider: {
    textAlign: 'center',
    margin: '24px 0',
    position: 'relative',
    color: '#888',
    fontSize: '14px',
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
    marginBottom: '20px',
    transition: 'all 0.2s',
  },
  footer: {
    textAlign: 'center',
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

## 🧪 Test the Registration

### Test with all fields:

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "012345678",
    "password": "password123",
    "confirmPassword": "password123",
    "dateOfBirth": "1990-01-15",
    "gender": "Male",
    "nationality": "Cambodia"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Account created successfully",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 24,
      "fullName": "John Doe",
      "username": "johndoe",
      "email": "john.doe@example.com",
      "phoneNumber": "+85512345678",
      "role": "USER",
      "dateOfBirth": "1990-01-15",
      "gender": "Male",
      "nationality": "Cambodia",
      "loginProvider": null
    }
  }
}
```

---

## 📊 Database View

After registration, check the database:

```sql
SELECT 
    id,
    username,
    full_name,
    email,
    phone_number,
    date_of_birth,
    gender,
    nationality,
    created_at
FROM users
ORDER BY created_at DESC
LIMIT 5;
```

---

## Summary

✅ **Backend updated** - Now accepts `dateOfBirth`, `gender`, `nationality`  
✅ **Complete React form** - All fields with validation  
✅ **OAuth signup** - Google and Facebook buttons included  
✅ **Responsive design** - Works on mobile and desktop  
✅ **Optional fields** - Users can skip personal information

Your registration form is ready to use! 🎉
