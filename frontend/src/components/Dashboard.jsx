import { useState, useEffect, useCallback } from 'react';
import ConnectProfiles from './ConnectProfiles';
import StatsPanel from './StatsPanel';
import TopicCards from './TopicCards';
import TopicProblemList from './TopicProblemList';
import RevisionStats from './RevisionStats';
import {
  markProblemRevised,
  getStats,
  getTopicsSummary,
  getRevisionStats,
} from '../api';

/**
 * Dashboard Component
 * 
 * Order:
 * 1. Top header with Logout
 * 2. Connect Profiles card (highlighted if no synced problems)
 * 3. Topic Cards grid (or selected Topic Problem List with back button)
 * 4. Revision statistics panel
 * 5. General preparation stats panel
 */
export default function Dashboard({ user, onLogout, onUnauthorized }) {
  // Overall preparation stats state (GET /api/stats)
  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(true);

  // Topic Cards & Problem List state (GET /api/topics/summary)
  const [topics, setTopics] = useState([]);
  const [topicsLoading, setTopicsLoading] = useState(true);
  const [selectedTopic, setSelectedTopic] = useState(null);

  // Revision Stats state (GET /api/revision/stats)
  const [revisionStats, setRevisionStats] = useState(null);
  const [revisionStatsLoading, setRevisionStatsLoading] = useState(true);

  // Error feedback state
  const [error, setError] = useState('');

  // 1. Fetch preparation stats
  const fetchStats = useCallback(async () => {
    setStatsLoading(true);
    try {
      const data = await getStats(onUnauthorized);
      setStats(data || null);
    } catch (err) {
      setError(err.message || 'Failed to fetch statistics.');
    } finally {
      setStatsLoading(false);
    }
  }, [onUnauthorized]);

  // 2. Fetch topic summary cards
  const fetchTopics = useCallback(async () => {
    setTopicsLoading(true);
    try {
      const data = await getTopicsSummary(onUnauthorized);
      setTopics(data || []);
    } catch {
      // Non-fatal
    } finally {
      setTopicsLoading(false);
    }
  }, [onUnauthorized]);

  // 3. Fetch revision statistics
  const fetchRevisionStats = useCallback(async () => {
    setRevisionStatsLoading(true);
    try {
      const data = await getRevisionStats(onUnauthorized);
      setRevisionStats(data || null);
    } catch {
      // Non-fatal
    } finally {
      setRevisionStatsLoading(false);
    }
  }, [onUnauthorized]);

  // Master refresh: synchronizes cards and stats after sync or revision
  const refreshAll = useCallback(() => {
    fetchTopics();
    fetchRevisionStats();
    fetchStats();
  }, [fetchTopics, fetchRevisionStats, fetchStats]);

  // Initial load
  useEffect(() => {
    refreshAll();
  }, [refreshAll]);

  // Revise action handler for topic problem list
  const handleMarkRevised = async (id) => {
    setError('');
    try {
      await markProblemRevised(id, onUnauthorized);
      refreshAll();
    } catch (err) {
      setError(err.message || 'Failed to update revision status.');
      throw err;
    }
  };

  // Determine if the user has no synced problems for empty state highlighting
  const hasNoSyncedProblems = !topicsLoading && (!topics || topics.length === 0);

  return (
    <div className="dashboard-container">
      {/* 1. Top Header with User Info and Logout */}
      <header className="dashboard-header">
        <div className="header-brand">
          <h1>DSA &amp; Placement Tracker</h1>
          <p className="user-welcome">
            Welcome back, <strong>{user?.name || user?.email || 'User'}</strong>
          </p>
        </div>
        <div className="header-actions">
          <button type="button" className="btn btn-outline" onClick={onLogout}>
            Logout
          </button>
        </div>
      </header>

      {/* Global Error Banner */}
      {error && <div className="alert alert-error">{error}</div>}

      {/* 2. Connect Profiles Card */}
      <section className="section-connect">
        <ConnectProfiles
          highlight={hasNoSyncedProblems}
          onSyncSuccess={refreshAll}
          onUnauthorized={onUnauthorized}
        />
      </section>

      {/* 3. Topic Cards Grid or Selected Topic Problem List */}
      <section className="section-topics">
        {selectedTopic ? (
          <TopicProblemList
            topic={selectedTopic}
            onBack={() => setSelectedTopic(null)}
            onRevise={handleMarkRevised}
            onProblemUpdated={refreshAll}
            onUnauthorized={onUnauthorized}
          />
        ) : (
          <TopicCards
            topics={topics}
            loading={topicsLoading}
            onSelectTopic={(t) => setSelectedTopic(t)}
          />
        )}
      </section>

      {/* 4. Revision Statistics Panel */}
      <section className="section-revision-stats">
        <RevisionStats stats={revisionStats} loading={revisionStatsLoading} />
      </section>

      {/* 5. Existing Preparation Stats Panel */}
      <section className="section-stats">
        <StatsPanel stats={stats} loading={statsLoading} />
      </section>
    </div>
  );
}
