import { useState, useEffect, useCallback } from 'react';
import { getTopicProblems } from '../api';

/**
 * TopicProblemList Component
 * 
 * Displays all problems for a selected topic:
 * 1) Suggested problem first and highlighted
 * 2) Revised problems by lastRevisedDate (most recent first)
 * 3) Never-revised problems by solvedDate (oldest first)
 * 4) Mastered problems last with a Mastered badge
 * 
 * Features:
 * - Back button to return to topic cards grid.
 * - Revise button: enabled if canRevise is true, or disabled with "Available in N days".
 * - Mastered badge for problems revised 3+ times.
 * - Refreshes cards, list, and stats on revision.
 */
export default function TopicProblemList({ topic, onBack, onRevise, onProblemUpdated, onUnauthorized }) {
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionLoadingId, setActionLoadingId] = useState(null);

  const fetchTopicProblems = useCallback(async () => {
    if (!topic) return;
    setLoading(true);
    setError('');
    try {
      const data = await getTopicProblems(topic, onUnauthorized);
      setProblems(data || []);
    } catch (err) {
      setError(err.message || `Failed to fetch problems for topic: ${topic}`);
    } finally {
      setLoading(false);
    }
  }, [topic, onUnauthorized]);

  useEffect(() => {
    fetchTopicProblems();
  }, [fetchTopicProblems]);

  const handleReviseClick = async (id) => {
    setActionLoadingId(id);
    setError('');
    try {
      await onRevise(id);
      // Reload topic problem list after successful revision
      await fetchTopicProblems();
      if (onProblemUpdated) {
        onProblemUpdated();
      }
    } catch (err) {
      setError(err.message || 'Failed to revise problem.');
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div className="card topic-problem-list-card">
      <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <button
              type="button"
              className="btn btn-outline btn-sm"
              onClick={onBack}
              title="Return to topic cards grid"
            >
              ← Back to Cards
            </button>
            <h3 style={{ margin: 0 }}>{topic} Problems</h3>
          </div>
          <p className="subtitle" style={{ marginTop: '4px' }}>
            Ordered with the suggested problem first, followed by revision recency and mastered questions.
          </p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="empty-state">
          <p className="loading-text">Loading {topic} problems...</p>
        </div>
      ) : problems.length === 0 ? (
        <div className="empty-state">
          <p className="empty-title">No problems found for {topic}</p>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="problems-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Difficulty</th>
                <th>Solved Date</th>
                <th>Revisions</th>
                <th>Last Revised</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {problems.map((prob) => {
                const isSuggested = prob.suggested;
                const isMastered = prob.mastered;
                const canRevise = prob.canRevise;
                const rowClass = isSuggested ? 'suggested-row' : '';

                return (
                  <tr key={prob.id} className={rowClass}>
                    {/* Title with Suggested & Mastered tags */}
                    <td className="col-title">
                      <div className="title-cell">
                        <div style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: '6px' }}>
                          {prob.link ? (
                            <a
                              href={prob.link}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="problem-link"
                            >
                              {prob.title} ↗
                            </a>
                          ) : (
                            <span className="problem-title-text">{prob.title}</span>
                          )}

                          {isSuggested && (
                            <span className="badge-suggested-tag">🎯 Suggested</span>
                          )}

                          {isMastered && (
                            <span className="badge badge-mastered">🏆 Mastered</span>
                          )}
                        </div>

                        {prob.notes && (
                          <p className="problem-notes-preview" title={prob.notes}>
                            💡 {prob.notes}
                          </p>
                        )}
                      </div>
                    </td>

                    {/* Difficulty */}
                    <td>
                      <span className={`badge badge-${(prob.difficulty || 'Easy').toLowerCase()}`}>
                        {prob.difficulty}
                      </span>
                    </td>

                    {/* Solved Date */}
                    <td>{prob.solvedDate || '—'}</td>

                    {/* Revision Count */}
                    <td>
                      <span className="revision-count">{prob.revisionCount || 0}</span>
                      {isMastered && <span style={{ fontSize: '11px', color: '#7e22ce', marginLeft: '4px' }}>(Mastered)</span>}
                    </td>

                    {/* Last Revised */}
                    <td>{prob.lastRevisedDate || 'Never'}</td>

                    {/* Action Button */}
                    <td>
                      {isMastered ? (
                        <span className="badge badge-mastered" title="Problem revised 3 times and mastered!">
                          ✓ Mastered
                        </span>
                      ) : canRevise ? (
                        <button
                          type="button"
                          className="btn btn-primary btn-sm"
                          disabled={actionLoadingId === prob.id}
                          onClick={() => handleReviseClick(prob.id)}
                          title="Revise this problem today"
                        >
                          {actionLoadingId === prob.id ? 'Revising...' : '✓ Revise'}
                        </button>
                      ) : (
                        <button
                          type="button"
                          className="btn btn-outline btn-sm"
                          disabled
                          style={{ cursor: 'not-allowed', opacity: 0.65 }}
                          title={`Locked. Available in ${prob.daysUntilRevisable} days.`}
                        >
                          Available in {prob.daysUntilRevisable} {prob.daysUntilRevisable === 1 ? 'day' : 'days'}
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

