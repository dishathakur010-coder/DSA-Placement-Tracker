import { useState } from 'react';
import { createProblem } from '../api';
import { TOPICS, DIFFICULTIES } from '../constants';

/**
 * ProblemForm Component
 * 
 * What it does:
 * - Provides a clean form to add a new problem to the tracker.
 * - Collects title, topic (from the predefined dropdown), difficulty, link, and notes.
 * - Submits to the backend POST /api/problems.
 * - Notifies the parent component when a problem is created.
 * 
 * What useState does here:
 * - Holds user input while typing/selecting (title, topic, difficulty, link, notes).
 * - Tracks whether the form is currently submitting (`loading`) to prevent duplicate clicks.
 * - Holds user-friendly error and success banners.
 */
export default function ProblemForm({ onProblemAdded, onUnauthorized }) {
  // Form input states
  const [title, setTitle] = useState('');
  const [topic, setTopic] = useState(TOPICS[0]); // Default to first topic ('Arrays')
  const [difficulty, setDifficulty] = useState(DIFFICULTIES[0]); // Default to 'Easy'
  const [link, setLink] = useState('');
  const [notes, setNotes] = useState('');

  // UI feedback states
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Frontend validation
    if (!title.trim()) {
      setError('Problem title is required.');
      return;
    }

    if (!difficulty) {
      setError('Please select a difficulty level (Easy, Medium, or Hard).');
      return;
    }

    setLoading(true);

    try {
      const payload = {
        title: title.trim(),
        topic: topic || null,
        difficulty: difficulty,
        link: link.trim() || null,
        notes: notes.trim() || null,
        status: 'Pending', // New problems start in Pending status
      };

      const created = await createProblem(payload, onUnauthorized);

      // Reset form fields
      setTitle('');
      setTopic(TOPICS[0]);
      setDifficulty(DIFFICULTIES[0]);
      setLink('');
      setNotes('');

      setSuccess('Problem added successfully!');
      setTimeout(() => setSuccess(''), 3500); // Auto-clear success message after 3.5s

      // Notify parent component to update problems table, stats, and revision list
      if (onProblemAdded) {
        onProblemAdded(created);
      }
    } catch (err) {
      setError(err.message || 'Failed to add problem. Please check your inputs.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card form-card">
      <div className="card-header">
        <h3>Add New Problem</h3>
        <p className="subtitle">Track a new question you solved or want to practice.</p>
      </div>

      {/* Friendly error alert */}
      {error && <div className="alert alert-error">{error}</div>}

      {/* Friendly success alert */}
      {success && <div className="alert alert-success">{success}</div>}

      <form onSubmit={handleSubmit} className="form-grid">
        {/* Title */}
        <div className="field full-width">
          <label htmlFor="problem-title">
            Problem Title <span className="required">*</span>
          </label>
          <input
            id="problem-title"
            type="text"
            placeholder="e.g. Two Sum, Reverse Linked List, LRU Cache"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            disabled={loading}
            required
          />
        </div>

        {/* Topic Dropdown */}
        <div className="field">
          <label htmlFor="problem-topic">
            Topic <span className="required">*</span>
          </label>
          <select
            id="problem-topic"
            value={topic}
            onChange={(e) => setTopic(e.target.value)}
            disabled={loading}
            required
          >
            {TOPICS.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </div>

        {/* Difficulty Dropdown */}
        <div className="field">
          <label htmlFor="problem-difficulty">
            Difficulty <span className="required">*</span>
          </label>
          <select
            id="problem-difficulty"
            value={difficulty}
            onChange={(e) => setDifficulty(e.target.value)}
            disabled={loading}
            required
          >
            {DIFFICULTIES.map((d) => (
              <option key={d} value={d}>
                {d}
              </option>
            ))}
          </select>
        </div>

        {/* Problem Link */}
        <div className="field full-width">
          <label htmlFor="problem-link">Problem Link (URL)</label>
          <input
            id="problem-link"
            type="url"
            placeholder="e.g. https://leetcode.com/problems/two-sum/"
            value={link}
            onChange={(e) => setLink(e.target.value)}
            disabled={loading}
          />
        </div>

        {/* Notes */}
        <div className="field full-width">
          <label htmlFor="problem-notes">Key Takeaways & Notes</label>
          <textarea
            id="problem-notes"
            rows="3"
            placeholder="e.g. Used two-pointer approach. Time: O(n), Space: O(1). Watch out for edge cases."
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            disabled={loading}
          />
        </div>

        <div className="field full-width form-actions">
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Adding Problem...' : '+ Add Problem'}
          </button>
        </div>
      </form>
    </div>
  );
}
