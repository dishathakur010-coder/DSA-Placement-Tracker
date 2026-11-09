/**
 * ReviseToday Component
 * 
 * What it does:
 * - Displays all solved problems that are due for spaced repetition revision today or earlier.
 * - Shows nextRevisionDate and daysOverdue for each due problem.
 * - Shows a "Revise" button that increments revisionCount and reschedules the next revision.
 * - Shows "You're all caught up!" when no problems are due.
 * 
 * What props are used:
 * - problemsDue: Array of ProblemRevisionResponse objects returned by GET /api/problems/revise-today.
 * - loading: Whether revision data is currently loading from the backend.
 * - onRevise: Callback function invoked when the "Revise" button is clicked.
 */
export default function ReviseToday({ problemsDue, loading, onRevise }) {
  return (
    <div className="card revise-card">
      <div className="card-header">
        <div className="header-with-badge">
          <h3>Revise Today</h3>
          {!loading && problemsDue && problemsDue.length > 0 && (
            <span className="count-badge count-badge-urgent">
              {problemsDue.length} due
            </span>
          )}
        </div>
        <p className="subtitle">
          Spaced-repetition revisions scheduled based on your solve history.
        </p>
      </div>

      {loading ? (
        <div className="empty-state">
          <p className="loading-text">Checking revision schedule...</p>
        </div>
      ) : !problemsDue || problemsDue.length === 0 ? (
        <div className="alert alert-success caught-up-box">
          <p className="caught-up-title">🎉 You're all caught up!</p>
          <p className="caught-up-text">
            No problems are due for revision today. Keep solving new problems or take a well-deserved break!
          </p>
        </div>
      ) : (
        <div className="revise-list">
          {problemsDue.map((prob) => (
            <div key={prob.id} className="revise-item">
              <div className="revise-item-info">
                <div className="revise-item-header">
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

                  <span className={`badge badge-${(prob.difficulty || 'Easy').toLowerCase()}`}>
                    {prob.difficulty}
                  </span>

                  <span className="topic-tag">{prob.topic || 'General'}</span>
                </div>

                <div className="revise-item-meta">
                  <span className="due-date">
                    📅 Scheduled: <strong>{prob.nextRevisionDate || 'Today'}</strong>
                  </span>
                  {prob.daysOverdue > 0 ? (
                    <span className="badge badge-overdue">
                      ⚠️ Overdue by {prob.daysOverdue} {prob.daysOverdue === 1 ? 'day' : 'days'}
                    </span>
                  ) : (
                    <span className="badge badge-due-today">
                      ⚡ Due Today
                    </span>
                  )}
                  <span className="past-revisions">
                    Past revisions: {prob.revisionCount || 0}
                  </span>
                </div>

                {prob.notes && (
                  <p className="problem-notes-preview" title={prob.notes}>
                    💡 {prob.notes}
                  </p>
                )}
              </div>

              <div className="revise-item-action">
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={() => onRevise(prob.id)}
                  title="Mark this problem revised today"
                >
                  ✓ Mark Revised
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

