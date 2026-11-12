import { useState, useEffect } from 'react';
import { getCodeforcesHandle, saveCodeforcesHandle, syncCodeforces } from '../api';

/**
 * ConnectProfiles Component
 * 
 * What it does:
 * - Allows the user to connect their Codeforces handle.
 * - Saves the handle to their profile (PUT /api/profile/codeforces).
 * - Syncs their solved problems from the last 60 days (POST /api/sync/codeforces).
 * - Shows clear success ("Imported N new problems") or friendly error banners.
 * - Refreshes the dashboard (problem list, stats, revise-today) immediately upon sync.
 */
export default function ConnectProfiles({ onSyncSuccess, onUnauthorized, highlight = false }) {
  const [handle, setHandle] = useState('');
  const [savedHandle, setSavedHandle] = useState('');

  // Loading states
  const [isLoadingHandle, setIsLoadingHandle] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isSyncing, setIsSyncing] = useState(false);

  // User feedback states
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Load existing handle when component mounts
  useEffect(() => {
    let isMounted = true;
    async function fetchSavedHandle() {
      setIsLoadingHandle(true);
      try {
        const data = await getCodeforcesHandle(onUnauthorized);
        if (isMounted && data && data.handle) {
          setHandle(data.handle);
          setSavedHandle(data.handle);
        }
      } catch {
        // Not a critical error if user has no handle saved yet
      } finally {
        if (isMounted) {
          setIsLoadingHandle(false);
        }
      }
    }

    fetchSavedHandle();
    return () => {
      isMounted = false;
    };
  }, [onUnauthorized]);

  // Handle Save
  const handleSave = async (e) => {
    if (e) e.preventDefault();
    setError('');
    setSuccess('');

    if (!handle.trim()) {
      setError('Please enter a Codeforces handle.');
      return;
    }

    setIsSaving(true);
    try {
      const result = await saveCodeforcesHandle(handle.trim(), onUnauthorized);
      setSavedHandle(result.handle || handle.trim());
      setSuccess('Codeforces handle saved successfully!');
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError(err.message || 'Failed to save Codeforces handle.');
    } finally {
      setIsSaving(false);
    }
  };

  // Handle Sync
  const handleSync = async () => {
    setError('');
    setSuccess('');

    if (!savedHandle && !handle.trim()) {
      setError('Please enter and save your Codeforces handle before syncing.');
      return;
    }

    // If the input doesn't match the saved handle, save it first
    if (handle.trim() && handle.trim() !== savedHandle) {
      try {
        setIsSaving(true);
        const result = await saveCodeforcesHandle(handle.trim(), onUnauthorized);
        setSavedHandle(result.handle || handle.trim());
      } catch (err) {
        setError(err.message || 'Failed to save handle before syncing.');
        setIsSaving(false);
        return;
      } finally {
        setIsSaving(false);
      }
    }

    setIsSyncing(true);
    try {
      const summary = await syncCodeforces(onUnauthorized);
      setSuccess(summary.message || `Imported ${summary.importedCount || 0} new problems`);
      // Notify parent to refresh Problems, Revise Today, and Stats
      if (onSyncSuccess) {
        onSyncSuccess();
      }
    } catch (err) {
      setError(err.message || 'Failed to sync with Codeforces. Please try again.');
    } finally {
      setIsSyncing(false);
    }
  };

  return (
    <div className={`card form-card connect-profiles-card ${highlight ? 'connect-profiles-highlighted' : ''}`}>
      <div className="card-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '18px' }}>⚡</span>
          <h3>Connect Profiles</h3>
        </div>
        <p className="subtitle">
          Link your competitive programming accounts to automatically import your solved problems.
        </p>
      </div>

      {/* Error Feedback */}
      {error && <div className="alert alert-error">{error}</div>}

      {/* Success Feedback */}
      {success && <div className="alert alert-success">{success}</div>}

      <form onSubmit={handleSave} className="connect-profiles-form">
        <div className="field full-width">
          <label htmlFor="codeforces-handle">
            Codeforces Handle {savedHandle && <span style={{ color: '#16a34a', fontWeight: '500', fontSize: '12px' }}>✓ Connected as {savedHandle}</span>}
          </label>
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}>
            <input
              id="codeforces-handle"
              type="text"
              placeholder="e.g. tourist, ecnerwala"
              value={handle}
              onChange={(e) => setHandle(e.target.value)}
              disabled={isLoadingHandle || isSaving || isSyncing}
              style={{ flex: '1', minWidth: '200px' }}
            />
            <button
              type="submit"
              className="btn btn-outline"
              disabled={isLoadingHandle || isSaving || isSyncing || !handle.trim()}
              title="Save this handle to your profile"
            >
              {isSaving ? 'Saving...' : 'Save'}
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleSync}
              disabled={isLoadingHandle || isSaving || isSyncing || (!handle.trim() && !savedHandle)}
              title="Import your full solve history from Codeforces"
            >
              {isSyncing ? 'Syncing...' : 'Sync now'}
            </button>
          </div>
          <span className="field-hint" style={{ fontSize: '12px', color: '#64748b', marginTop: '4px' }}>
            Imports your full solve history from Codeforces and maps ratings to Easy/Medium/Hard.
          </span>
        </div>
      </form>
    </div>
  );
}

