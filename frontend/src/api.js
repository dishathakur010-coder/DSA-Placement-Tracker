// Storage keys for persisting authentication in the browser
const TOKEN_KEY = 'tracker_token';
const USER_KEY = 'tracker_user';

// Helpers to get, set, and remove auth data from localStorage
export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getStoredUser() {
  const user = localStorage.getItem(USER_KEY);
  try {
    return user ? JSON.parse(user) : null;
  } catch {
    return null;
  }
}

export function saveAuthData(token, user) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAuthData() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

/**
 * Core helper function for all API calls:
 * 1. Attaches the Bearer token in the Authorization header.
 * 2. On 401 Unauthorized for protected routes, clears localStorage and redirects to login.
 * 3. On 401 for auth routes (login), surfaces "Bad credentials" / "Invalid email or password".
 * 4. Formats backend validation and error messages into friendly readable strings.
 */
export async function apiFetch(endpoint, options = {}, onUnauthorized = null) {
  const token = getStoredToken();

  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(endpoint, {
    ...options,
    headers,
  });

  // 204 No Content (e.g. DELETE requests) has no response body
  if (response.status === 204) {
    return null;
  }

  // Parse response body
  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  const data = isJson ? await response.json() : await response.text();

  // Handle 401 Unauthorized
  if (response.status === 401) {
    clearAuthData();

    // If attempting to log in, 401 means Bad Credentials (wrong email or password)
    if (endpoint.includes('/api/auth/')) {
      let authError = 'Invalid email or password. Please try again.';
      if (data && typeof data === 'object' && data.message) {
        authError = data.message;
      }
      throw new Error(authError);
    }

    // For protected routes (/api/problems, etc.), 401 means the token is expired/invalid
    if (onUnauthorized) {
      onUnauthorized();
    }
    window.dispatchEvent(new Event('auth:unauthorized'));
    throw new Error('Your session has expired. Please log in again.');
  }

  // If the server responded with any other error (status 400, 404, 500, etc.)
  if (!response.ok) {
    let friendlyMessage = 'Something went wrong. Please try again.';

    if (data && typeof data === 'object') {
      // Spring Boot GlobalExceptionHandler returns:
      // { "message": "Validation failed", "errors": { "title": "Title cannot be blank" } }
      if (data.errors && typeof data.errors === 'object') {
        const fieldErrors = Object.values(data.errors);
        if (fieldErrors.length > 0) {
          friendlyMessage = fieldErrors.join('. ');
        }
      } else if (data.message) {
        friendlyMessage = data.message;
      } else if (data.error) {
        friendlyMessage = data.error;
      }
    } else if (typeof data === 'string' && data.trim().length > 0) {
      friendlyMessage = data;
    }

    throw new Error(friendlyMessage);
  }

  return data;
}

// -------------------------------------------------------------
// Authentication Endpoints
// -------------------------------------------------------------

export async function loginUser(email, password) {
  return apiFetch('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}

export async function registerUser(name, email, password) {
  return apiFetch('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ name, email, password }),
  });
}

// -------------------------------------------------------------
// Problem Endpoints
// -------------------------------------------------------------

export async function getProblems(filters = {}, onUnauthorized = null) {
  const params = new URLSearchParams();
  if (filters.topic) params.append('topic', filters.topic);
  if (filters.difficulty) params.append('difficulty', filters.difficulty);
  if (filters.status) params.append('status', filters.status);

  const queryString = params.toString();
  const endpoint = queryString ? `/api/problems?${queryString}` : '/api/problems';

  return apiFetch(endpoint, { method: 'GET' }, onUnauthorized);
}

export async function createProblem(problemData, onUnauthorized = null) {
  return apiFetch(
    '/api/problems',
    {
      method: 'POST',
      body: JSON.stringify(problemData),
    },
    onUnauthorized
  );
}

export async function deleteProblem(id, onUnauthorized = null) {
  return apiFetch(
    `/api/problems/${id}`,
    {
      method: 'DELETE',
    },
    onUnauthorized
  );
}

export async function markProblemSolved(id, onUnauthorized = null) {
  return apiFetch(
    `/api/problems/${id}/solve`,
    {
      method: 'PATCH',
    },
    onUnauthorized
  );
}

export async function markProblemRevised(id, onUnauthorized = null) {
  return apiFetch(
    `/api/problems/${id}/revise`,
    {
      method: 'PATCH',
    },
    onUnauthorized
  );
}

// -------------------------------------------------------------
// Revise Today & Stats Endpoints
// -------------------------------------------------------------

export async function getProblemsDueForRevision(onUnauthorized = null) {
  return apiFetch('/api/problems/revise-today', { method: 'GET' }, onUnauthorized);
}

export async function getStats(onUnauthorized = null) {
  return apiFetch('/api/stats', { method: 'GET' }, onUnauthorized);
}

// -------------------------------------------------------------
// Codeforces Profile & Sync Endpoints
// -------------------------------------------------------------

export async function getCodeforcesHandle(onUnauthorized = null) {
  return apiFetch('/api/profile/codeforces', { method: 'GET' }, onUnauthorized);
}

export async function saveCodeforcesHandle(handle, onUnauthorized = null) {
  return apiFetch(
    '/api/profile/codeforces',
    {
      method: 'PUT',
      body: JSON.stringify({ handle }),
    },
    onUnauthorized
  );
}

export async function syncCodeforces(onUnauthorized = null) {
  return apiFetch(
    '/api/sync/codeforces',
    {
      method: 'POST',
    },
    onUnauthorized
  );
}

// -------------------------------------------------------------
// Topic Cards, Problem List, & Revision Stats Endpoints
// -------------------------------------------------------------

export async function getTopicsSummary(onUnauthorized = null) {
  return apiFetch('/api/topics/summary', { method: 'GET' }, onUnauthorized);
}

export async function getTopicProblems(topic, onUnauthorized = null) {
  const encodedTopic = encodeURIComponent(topic);
  return apiFetch(`/api/topics/problems?topic=${encodedTopic}`, { method: 'GET' }, onUnauthorized);
}

export async function getRevisionStats(onUnauthorized = null) {
  return apiFetch('/api/revision/stats', { method: 'GET' }, onUnauthorized);
}
