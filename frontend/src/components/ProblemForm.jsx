import { useState } from 'react';
import { createProblem } from '../api';

/**
 * ProblemForm Component
 * 
 * What it does:
 * - Provides a clean form to add a new problem to the tracker.
 * - Collects title, topic, difficulty (Easy/Medium/Hard), link, and notes.
 * - Submits to the backend POST /api/problems.
 * - Notifies the parent component when a problem is created so the table updates instantly.
 * 
 * What useState does here:
 * - Tracks what the user has typed into each form field (title, topic, etc.).
 * - Tracks UI feedback: loading state (disabling the button), error message, and success message.
 */
export default function ProblemForm({ onProblemAdded, onUnauthorized }) {
  // Form input states (matching backend ProblemRequest fields)
  const [title, setTitle] = useState('');
  const [topic, setTopic] = useState('');
  const [difficulty, setDifficulty] = useState('Easy');
  const [link, setLink] = useState('');
  const [notes, setNotes] = useState('');

  // UI status states
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
        topic: topic.trim() || null,
        difficulty: difficulty,
        link: link.trim() || null,
        notes: notes.trim() || null,
        status: 'Pending', // New problems start in Pending status
      };

      const created = await createProblem(payload, onUnauthorized);

      // Reset form fields
      setTitle('');
      setTopic('');
      setDifficulty('Easy');
      setLink('');
      setNotes('');

      setSuccess('Problem added successfully!');
      setTimeout(() => setSuccess(''), 3500); // Clear success message after 3.5s

      // Notify parent component to update the problems table
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

        {/* Topic */}
        <div className="field">
          <label htmlFor="problem-topic">Topic</label>
          <input
            id="problem-topic"
            type="text"
            placeholder="e.g. Arrays, Trees, Dynamic Programming"
            value={topic}
            onChange={(e) => setTopic(e.target.value)}
            disabled={loading}
          />
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
            <option value="Easy">Easy</option>
            <option value="Medium">Medium</option>
            <option value="Hard">Hard</option>
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
            placeholder="e.g. Used two-pointer approach. Time: O(n), Space: O(1). Watch out for negative numbers."
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

