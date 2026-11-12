import { useState, useEffect, useCallback } from 'react';
import ProblemForm from './ProblemForm';
import ConnectProfiles from './ConnectProfiles';
import ProblemList from './ProblemList';
import StatsPanel from './StatsPanel';
import TopicCards from './TopicCards';
import TopicProblemList from './TopicProblemList';
import RevisionStats from './RevisionStats';
import {
  getProblems,
  deleteProblem,
  markProblemSolved,
  markProblemRevised,
  getStats,
  getTopicsSummary,
  getRevisionStats,
} from '../api';

/**
 * Dashboard Component
 * 
 * Coordinates:
 * 1. StatsPanel (general preparation analytics & weakest topic)
 * 2. RevisionStats (separate revision numbers & topic strength score bars)
 * 3. TopicCards / TopicProblemList (interactive cards grid & suggested problem view)
 * 4. ConnectProfiles (Codeforces full history sync)
 * 5. ProblemForm (manual problem addition)
 * 6. ProblemList (filter bar + full problems table)
 */
export default function Dashboard({ user, onLogout, onUnauthorized }) {
  // Problems table state
  const [problems, setProblems] = useState([]);
  const [problemsLoading, setProblemsLoading] = useState(true);

  // Active filters state
  const [filters, setFilters] = useState({
    topic: '',
    difficulty: '',
    status: '',
  });

  // Overall prep stats state
  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(true);

  // Topic Cards & Problem List state
  const [topics, setTopics] = useState([]);
  const [topicsLoading, setTopicsLoading] = useState(true);
  const [selectedTopic, setSelectedTopic] = useState(null);

  // Revision Stats state
  const [revisionStats, setRevisionStats] = useState(null);
  const [revisionStatsLoading, setRevisionStatsLoading] = useState(true);

  // Error feedback state
  const [error, setError] = useState('');

  // 1. Fetch filtered problems from GET /api/problems?topic=&difficulty=&status=
  const fetchProblems = useCallback(
    async (currentFilters = filters) => {
      setProblemsLoading(true);
      try {
        const data = await getProblems(currentFilters, onUnauthorized);
        setProblems(data || []);
      } catch (err) {
        setError(err.message || 'Failed to fetch problems.');
      } finally {
        setProblemsLoading(false);
      }
    },
    [filters, onUnauthorized]
  );

  // 2. Fetch statistics from GET /api/stats
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

  // 3. Fetch topic cards summary from GET /api/topics/summary
  const fetchTopics = useCallback(async () => {
    setTopicsLoading(true);
    try {
      const data = await getTopicsSummary(onUnauthorized);
      setTopics(data || []);
    } catch (err) {
      // Non-fatal
    } finally {
      setTopicsLoading(false);
    }
  }, [onUnauthorized]);

  // 4. Fetch revision stats from GET /api/revision/stats
  const fetchRevisionStats = useCallback(async () => {
    setRevisionStatsLoading(true);
    try {
      const data = await getRevisionStats(onUnauthorized);
      setRevisionStats(data || null);
    } catch (err) {
      // Non-fatal
    } finally {
      setRevisionStatsLoading(false);
    }
  }, [onUnauthorized]);

  // Master refresh function: synchronizes all sections
  const refreshAll = useCallback(
    (currentFilters = filters) => {
      fetchProblems(currentFilters);
      fetchStats();
      fetchTopics();
      fetchRevisionStats();
    },
    [fetchProblems, fetchStats, fetchTopics, fetchRevisionStats, filters]
  );

  // Initial load
  useEffect(() => {
    refreshAll(filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Filter handlers
  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
    fetchProblems(newFilters);
  };

  const handleClearFilters = () => {
    const cleared = { topic: '', difficulty: '', status: '' };
    setFilters(cleared);
    fetchProblems(cleared);
  };

  const handleProblemAdded = () => {
    refreshAll(filters);
  };

  // Called when "Solve" is clicked
  const handleMarkSolved = async (id) => {
    setError('');
    try {
      await markProblemSolved(id, onUnauthorized);
      refreshAll(filters);
    } catch (err) {
      setError(err.message || 'Failed to mark problem as solved.');
    }
  };

  // Called when "Revise" is clicked
  const handleMarkRevised = async (id) => {
    setError('');
    try {
      await markProblemRevised(id, onUnauthorized);
      refreshAll(filters);
    } catch (err) {
      setError(err.message || 'Failed to update revision status.');
      throw err;
    }
  };

  // Called when "Delete" is clicked
  const handleDelete = async (id) => {
    const confirmed = window.confirm(
      'Are you sure you want to delete this problem?'
    );
    if (!confirmed) return;

    setError('');
    try {
      await deleteProblem(id, onUnauthorized);
      refreshAll(filters);
    } catch (err) {
      setError(err.message || 'Failed to delete problem.');
    }
  };

  return (
    <div className="dashboard-container">
      {/* Top Header */}
      <header className="dashboard-header">
        <div className="header-brand">
          <h1>DSA & Placement Tracker</h1>
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

      {/* 1. Preparation Stats Panel */}
      <section className="section-stats">
        <StatsPanel stats={stats} loading={statsLoading} />
      </section>

      {/* 2. Revision Statistics Panel */}
      <section className="section-revision-stats">
        <RevisionStats stats={revisionStats} loading={revisionStatsLoading} />
      </section>

      {/* 3. Topic Cards Grid or Selected Topic Problem List */}
      <section className="section-topics">
        {selectedTopic ? (
          <TopicProblemList
            topic={selectedTopic}
            onBack={() => setSelectedTopic(null)}
            onRevise={handleMarkRevised}
            onProblemUpdated={() => refreshAll(filters)}
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

      {/* 4. Connect Profiles Card */}
      <section className="section-connect">
        <ConnectProfiles
          onSyncSuccess={() => refreshAll(filters)}
          onUnauthorized={onUnauthorized}
        />
      </section>

      {/* 5. Add Problem Form */}
      <section className="section-form">
        <ProblemForm
          onProblemAdded={handleProblemAdded}
          onUnauthorized={onUnauthorized}
        />
      </section>

      {/* 6. Problems Table with Filter Bar */}
      <section className="section-list">
        <ProblemList
          problems={problems}
          loading={problemsLoading}
          filters={filters}
          onFilterChange={handleFilterChange}
          onClearFilters={handleClearFilters}
          onMarkSolved={handleMarkSolved}
          onMarkRevised={handleMarkRevised}
          onDelete={handleDelete}
        />
      </section>
    </div>
  );
}
