import { useState, useEffect } from 'react';
import AuthForm from './components/AuthForm';
import Dashboard from './components/Dashboard';
import { getStoredToken, getStoredUser, clearAuthData } from './api';
import './App.css';

/**
 * Main App Component
 * 
 * What it does:
 * - Serves as the root component of the application.
 * - Manages global authentication state (token and user).
 * - Implements conditional rendering (Task 7A requirement #7):
 *     If logged in (token exists)  -> shows the Dashboard.
 *     If logged out (token is null) -> shows the Login/Register form.
 * 
 * What useState does here:
 * - Initializes `token` and `user` by reading any saved session from localStorage.
 * - Calling `setToken(...)` switches the entire view between Auth and Dashboard instantly.
 * 
 * What useEffect does here:
 * - Subscribes to the 'auth:unauthorized' window event emitted when a 401 response is caught.
 * - Automatically logs the user out if their JWT token expires.
 */
function App() {
  // Read initial auth state from localStorage
  const [token, setToken] = useState(() => getStoredToken());
  const [user, setUser] = useState(() => getStoredUser());

  // Called when login or registration is successful
  const handleAuthSuccess = (newToken, newUser) => {
    setToken(newToken);
    setUser(newUser);
  };

  // Called when the user clicks "Logout" or receives a 401 Unauthorized
  const handleLogout = () => {
    clearAuthData();
    setToken(null);
    setUser(null);
  };

  // Listen for 401 Unauthorized events from api.js
  useEffect(() => {
    const onUnauthorizedEvent = () => {
      handleLogout();
    };

    window.addEventListener('auth:unauthorized', onUnauthorizedEvent);

    // Cleanup listener when component unmounts
    return () => {
      window.removeEventListener('auth:unauthorized', onUnauthorizedEvent);
    };
  }, []);

  return (
    <div className="app-container">
      {token ? (
        // When a token exists, render the Dashboard
        <Dashboard
          user={user}
          onLogout={handleLogout}
          onUnauthorized={handleLogout}
        />
      ) : (
        // When not logged in, render the Login / Register form
        <main className="auth-wrapper">
          <header className="app-branding">
            <h1>DSA & Placement Tracker</h1>
            <p className="app-tagline">
              Master Data Structures & Algorithms, manage your revisions, and ace your interviews.
            </p>
          </header>
          <AuthForm onAuthSuccess={handleAuthSuccess} />
        </main>
      )}
    </div>
  );
}

export default App;
