import { useState, useEffect, useCallback } from 'react';
import ProblemForm from './ProblemForm';
import ConnectProfiles from './ConnectProfiles';
import ProblemList from './ProblemList';
import ReviseToday from './ReviseToday';
import StatsPanel from './StatsPanel';
import {
  getProblems,
  deleteProblem,
  markProblemSolved,
  markProblemRevised,
  getProblemsDueForRevision,
  getStats,
} from '../api';

/**
 * Dashboard Component
 * 
 * What it does:
 * - Coordinates all four main panels:
 *     1. StatsPanel (real-time progress, topic bars, weakest topic)
 *     2. ReviseToday (spaced repetition items due today with overdue calculation)
 *     3. ProblemForm (add new problem with topic dropdown)
 *     4. ProblemList (filter bar + problems table with Solve, Revise, Delete)
 * - Refreshes all data whenever any problem is added, solved, revised, or deleted.
 * 
 * What useState does here:
 * - Keeps separate state for problems, revise problems, and stats.
 * - Keeps track of loading states for each panel so users see loading indicators.
 * - Stores the active filter values (topic, difficulty, status).
 * 
 * What useEffect does here:
 * - Runs once on mount to fetch problems, revision list, and stats simultaneously.
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

  // Revise Today state
  const [reviseProblems, setReviseProblems] = useState([]);
  const [reviseLoading, setReviseLoading] = useState(true);

  // Stats state
  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(true);

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

  // 2. Fetch problems due for revision today from GET /api/problems/revise-today
  const fetchReviseToday = useCallback(async () => {
    setReviseLoading(true);
    try {
      const data = await getProblemsDueForRevision(onUnauthorized);
      setReviseProblems(data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch revision schedule.');
    } finally {
      setReviseLoading(false);
    }
  }, [onUnauthorized]);

  // 3. Fetch statistics from GET /api/stats
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

  // Master refresh function: synchronizes all three sections
  const refreshAll = useCallback(
    (currentFilters = filters) => {
      fetchProblems(currentFilters);
      fetchReviseToday();
      fetchStats();
    },
    [fetchProblems, fetchReviseToday, fetchStats, filters]
  );

  // Load everything on initial mount
  useEffect(() => {
    refreshAll(filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Filter change handler
  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
    fetchProblems(newFilters);
  };

  // Clear filters handler
  const handleClearFilters = () => {
    const cleared = { topic: '', difficulty: '', status: '' };
    setFilters(cleared);
    fetchProblems(cleared);
  };

  // Called when a new problem is created
  const handleProblemAdded = () => {
    refreshAll(filters);
  };

  // Called when "Solve" is clicked
  const handleMarkSolved = async (id) => {
    setError('');
    try {
      await markProblemSolved(id, onUnauthorized);
      // Refresh table, stats, and revision list after action
      refreshAll(filters);
    } catch (err) {
      setError(err.message || 'Failed to mark problem as solved.');
    }
  };

  // Called when "Revise" is clicked (from table or Revise Today panel)
  const handleMarkRevised = async (id) => {
    setError('');
    try {
      await markProblemRevised(id, onUnauthorized);
      // Refresh table, stats, and revision list after action
      refreshAll(filters);
    } catch (err) {
      setError(err.message || 'Failed to update revision status.');
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
      // Refresh table, stats, and revision list after action
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

      {/* 2. Revise Today Panel */}
      <section className="section-revise">
        <ReviseToday
          problemsDue={reviseProblems}
          loading={reviseLoading}
          onRevise={handleMarkRevised}
        />
      </section>

      {/* Connect Profiles Card */}
      <section className="section-connect">
        <ConnectProfiles
          onSyncSuccess={() => refreshAll(filters)}
          onUnauthorized={onUnauthorized}
        />
      </section>

      {/* 3. Add Problem Form */}
      <section className="section-form">
        <ProblemForm
          onProblemAdded={handleProblemAdded}
          onUnauthorized={onUnauthorized}
        />
      </section>

      {/* 4. Problems Table with Filter Bar */}
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
