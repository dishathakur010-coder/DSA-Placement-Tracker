import { useState } from 'react';
import { loginUser, registerUser, saveAuthData } from '../api';

/**
 * AuthForm Component
 * 
 * What it does:
 * - Provides a clean form for either Logging In or Registering a new account.
 * - Saves the JWT token and user info to localStorage upon success.
 * - Shows friendly error messages if validation fails (e.g. wrong password or duplicate email).
 * 
 * What useState does here:
 * - Each input has a corresponding state variable (name, email, password).
 * - React uses "controlled inputs", meaning the input value is bound to the state,
 *   and typing in the box calls the setter function (setName, setEmail, setPassword).
 */
export default function AuthForm({ onAuthSuccess }) {
  // isLogin is true for Login mode, and false for Register mode
  const [isLogin, setIsLogin] = useState(true);

  // Form input states
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // UI feedback states
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Toggle between Login and Register views
  const handleToggleMode = () => {
    setIsLogin((prev) => !prev);
    setError(''); // Clear any previous error when switching
  };

  // Handles form submission
  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevents the browser from reloading the page
    setError('');

    // Basic frontend checks before sending to backend
    if (!email.trim() || !password.trim()) {
      setError('Please fill in both email and password.');
      return;
    }

    if (!isLogin && !name.trim()) {
      setError('Please enter your name.');
      return;
    }

    if (!isLogin && password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }

    setLoading(true);

    try {
      let authResponse;
      if (isLogin) {
        authResponse = await loginUser(email.trim(), password);
      } else {
        authResponse = await registerUser(name.trim(), email.trim(), password);
      }

      // Backend returns AuthResponse: { token, type: 'Bearer', email, name }
      const token = authResponse.token;
      const user = {
        name: authResponse.name || name.trim() || 'User',
        email: authResponse.email || email.trim(),
      };

      // Save token and user details to localStorage
      saveAuthData(token, user);

      // Notify parent App component that authentication succeeded
      onAuthSuccess(token, user);
    } catch (err) {
      setError(err.message || 'Authentication failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-card">
      <div className="auth-header">
        <h2>{isLogin ? 'Welcome Back!' : 'Create an Account'}</h2>
        <p>
          {isLogin
            ? 'Log in to track your DSA questions and placement progress.'
            : 'Register to start tracking your DSA journey.'}
        </p>
      </div>

      {/* Friendly error banner if an error occurred */}
      {error && <div className="alert alert-error">{error}</div>}

      <form onSubmit={handleSubmit} className="form-group">
        {/* Only show Name field when registering */}
        {!isLogin && (
          <div className="field">
            <label htmlFor="name">Full Name</label>
            <input
              id="name"
              type="text"
              placeholder="e.g. John Doe"
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={loading}
              required
            />
          </div>
        )}

        <div className="field">
          <label htmlFor="email">Email Address</label>
          <input
            id="email"
            type="email"
            placeholder="e.g. student@college.edu"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={loading}
            required
          />
        </div>

        <div className="field">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            placeholder={isLogin ? 'Enter password' : 'At least 6 characters'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={loading}
            required
          />
        </div>

        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading
            ? isLogin
              ? 'Logging in...'
              : 'Creating account...'
            : isLogin
            ? 'Sign In'
            : 'Register'}
        </button>
      </form>

      <div className="auth-footer">
        <p>
          {isLogin ? "Don't have an account yet?" : 'Already registered?'}
          <button
            type="button"
            className="btn-link"
            onClick={handleToggleMode}
            disabled={loading}
          >
            {isLogin ? 'Create one now' : 'Sign in here'}
          </button>
        </p>
      </div>
    </div>
  );
}

