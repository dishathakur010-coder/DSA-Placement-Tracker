/**
 * ProblemList Component
 * 
 * What it does:
 * - Renders a table of all problems owned by the logged-in user.
 * - Displays difficulty badges (Easy, Medium, Hard) and status tags (Pending, Solved).
 * - Provides interactive buttons to mark problems as Solved, Revise them, or Delete them.
 * 
 * What props are used:
 * - problems: The array of problems received from the backend.
 * - loading: Whether problems are currently being fetched.
 * - onMarkSolved, onMarkRevised, onDelete: Callback functions triggered when action buttons are clicked.
 */
export default function ProblemList({
  problems,
  loading,
  onMarkSolved,
  onMarkRevised,
  onDelete,
}) {
  if (loading) {
    return (
      <div className="card list-card">
        <div className="empty-state">
          <p>Loading your problems...</p>
        </div>
      </div>
    );
  }

  if (!problems || problems.length === 0) {
    return (
      <div className="card list-card">
        <div className="card-header">
          <h3>Your Problems (0)</h3>
        </div>
        <div className="empty-state">
          <p className="empty-title">No problems tracked yet!</p>
          <p className="empty-desc">
            Use the form above to add your first LeetCode, Codeforces, or practice question.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="card list-card">
      <div className="card-header">
        <div className="header-with-badge">
          <h3>Your Problems</h3>
          <span className="count-badge">{problems.length} total</span>
        </div>
      </div>

      <div className="table-responsive">
        <table className="problems-table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Topic</th>
              <th>Difficulty</th>
              <th>Status</th>
              <th>Revisions</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {problems.map((problem) => {
              const isSolved =
                problem.status && problem.status.toLowerCase() === 'solved';

              return (
                <tr key={problem.id}>
                  {/* Title & Link */}
                  <td className="col-title">
                    <div className="title-cell">
                      {problem.link ? (
                        <a
                          href={problem.link}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="problem-link"
                          title="Open problem link in new tab"
                        >
                          {problem.title} ↗
                        </a>
                      ) : (
                        <span className="problem-title-text">
                          {problem.title}
                        </span>
                      )}

                      {/* Display notes preview if present */}
                      {problem.notes && (
                        <p className="problem-notes-preview" title={problem.notes}>
                          💡 {problem.notes}
                        </p>
                      )}
                    </div>
                  </td>

                  {/* Topic */}
                  <td className="col-topic">
                    <span className="topic-tag">
                      {problem.topic || 'General'}
                    </span>
                  </td>

                  {/* Difficulty Badge */}
                  <td className="col-difficulty">
                    <span
                      className={`badge badge-${(
                        problem.difficulty || 'Easy'
                      ).toLowerCase()}`}
                    >
                      {problem.difficulty}
                    </span>
                  </td>

                  {/* Status */}
                  <td className="col-status">
                    <span
                      className={`status-pill ${
                        isSolved ? 'status-solved' : 'status-pending'
                      }`}
                    >
                      {isSolved ? '✓ Solved' : '⏳ Pending'}
                    </span>
                  </td>

                  {/* Revision Count */}
                  <td className="col-revisions">
                    <span className="revision-count">
                      {problem.revisionCount || 0}
                    </span>
                  </td>

                  {/* Actions */}
                  <td className="col-actions">
                    <div className="action-buttons">
                      {!isSolved && (
                        <button
                          type="button"
                          className="btn-action btn-solve"
                          title="Mark problem as Solved"
                          onClick={() => onMarkSolved(problem.id)}
                        >
                          Mark Solved
                        </button>
                      )}

                      <button
                        type="button"
                        className="btn-action btn-revise"
                        title="Mark problem revised today"
                        onClick={() => onMarkRevised(problem.id)}
                      >
                        Revise
                      </button>

                      <button
                        type="button"
                        className="btn-action btn-delete"
                        title="Delete problem"
                        onClick={() => onDelete(problem.id)}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

