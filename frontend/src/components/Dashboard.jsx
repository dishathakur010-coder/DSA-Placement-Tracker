import { useState, useEffect } from 'react';
import ProblemForm from './ProblemForm';
import ProblemList from './ProblemList';
import {
  getProblems,
  deleteProblem,
  markProblemSolved,
  markProblemRevised,
} from '../api';

/**
 * Dashboard Component
 * 
 * What it does:
 * - Serves as the central hub for the authenticated user.
 * - Displays the header with the user's name and a "Logout" button.
 * - Manages the list of problems in state.
 * - Holds the Add Problem form and the Problems table.
 * 
 * What useState does here:
 * - problems: Stores the array of problems returned from Spring Boot.
 * - loading: Indicates whether we are currently fetching data from the backend.
 * - error: Stores any error message if an operation (fetching, deleting, etc.) fails.
 * 
 * What useEffect does here:
 * - useEffect(() => { loadProblems(); }, []); runs once when Dashboard mounts.
 * - This fetches the user's problems as soon as they log in and enter the dashboard.
 */
export default function Dashboard({ user, onLogout, onUnauthorized }) {
  // State for problems, loading, and error
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Fetch problems from GET /api/problems
  const loadProblems = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getProblems(onUnauthorized);
      // data is an array of ProblemResponse objects
      setProblems(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load problems from server.');
    } finally {
      setLoading(false);
    }
  };

  // Run once when this Dashboard component appears on the screen
  useEffect(() => {
    loadProblems();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Called when a new problem is created by ProblemForm
  const handleProblemAdded = (newProblem) => {
    // Add the new problem to the top of our state list
    setProblems((prevProblems) => [newProblem, ...prevProblems]);
  };

  // Called when "Mark Solved" is clicked
  const handleMarkSolved = async (id) => {
    setError('');
    try {
      const updated = await markProblemSolved(id, onUnauthorized);
      // Replace the old problem object with the updated one from backend
      setProblems((prev) =>
        prev.map((prob) => (prob.id === id ? updated : prob))
      );
    } catch (err) {
      setError(err.message || 'Failed to mark problem as solved.');
    }
  };

  // Called when "Revise" is clicked
  const handleMarkRevised = async (id) => {
    setError('');
    try {
      const updated = await markProblemRevised(id, onUnauthorized);
      // Replace the old problem with updated revisionCount and lastRevisedDate
      setProblems((prev) =>
        prev.map((prob) => (prob.id === id ? updated : prob))
      );
    } catch (err) {
      setError(err.message || 'Failed to update revision status.');
    }
  };

  // Called when "Delete" is clicked
  const handleDelete = async (id) => {
    const confirmed = window.confirm('Are you sure you want to delete this problem?');
    if (!confirmed) return;

    setError('');
    try {
      await deleteProblem(id, onUnauthorized);
      // Filter out the deleted problem from state
      setProblems((prev) => prev.filter((prob) => prob.id !== id));
    } catch (err) {
      setError(err.message || 'Failed to delete problem.');
    }
  };

  return (
    <div className="dashboard-container">
      {/* Top Navigation / Header */}
      <header className="dashboard-header">
        <div className="header-brand">
          <h1>DSA & Placement Tracker</h1>
          <p className="user-welcome">
            Logged in as <strong>{user?.name || user?.email || 'User'}</strong>
          </p>
        </div>
        <div className="header-actions">
          <button type="button" className="btn btn-outline" onClick={onLogout}>
            Logout
          </button>
        </div>
      </header>

      {/* Global Dashboard Error Banner */}
      {error && <div className="alert alert-error">{error}</div>}

      {/* Add Problem Form */}
      <section className="section-form">
        <ProblemForm
          onProblemAdded={handleProblemAdded}
          onUnauthorized={onUnauthorized}
        />
      </section>

      {/* Problems Table */}
      <section className="section-list">
        <ProblemList
          problems={problems}
          loading={loading}
          onMarkSolved={handleMarkSolved}
          onMarkRevised={handleMarkRevised}
          onDelete={handleDelete}
        />
      </section>
    </div>
  );
}

