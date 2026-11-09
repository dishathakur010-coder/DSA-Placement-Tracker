import FilterBar from './FilterBar';

/**
 * ProblemList Component
 * 
 * What it does:
 * - Renders the FilterBar and the table of problems.
 * - Each row has:
 *     - Solve button (only for unsolved problems)
 *     - Revise button
 *     - Delete button (with confirmation popup)
 * - Shows loading text while fetching.
 * - Shows appropriate empty states (differentiating between no problems at all vs no problems matching active filters).
 */
export default function ProblemList({
  problems,
  loading,
  filters,
  onFilterChange,
  onClearFilters,
  onMarkSolved,
  onMarkRevised,
  onDelete,
}) {
  const hasActiveFilters = Boolean(
    filters && (filters.topic || filters.difficulty || filters.status)
  );

  return (
    <div className="card list-card">
      <div className="card-header">
        <div className="header-with-badge">
          <h3>Problem Tracker</h3>
          {!loading && (
            <span className="count-badge">
              {problems.length} {problems.length === 1 ? 'problem' : 'problems'}
            </span>
          )}
        </div>
        <p className="subtitle">
          View, filter, solve, and schedule revisions for your DSA practice.
        </p>
      </div>

      {/* Filter Bar */}
      <FilterBar
        filters={filters}
        onFilterChange={onFilterChange}
        onClear={onClearFilters}
      />

      {/* Loading State */}
      {loading ? (
        <div className="empty-state">
          <p className="loading-text">⏳ Loading your problems from server...</p>
        </div>
      ) : problems.length === 0 ? (
        /* Empty State */
        <div className="empty-state">
          {hasActiveFilters ? (
            <>
              <p className="empty-title">No problems match your filters</p>
              <p className="empty-desc">
                Try selecting different filters or click below to view all problems.
              </p>
              <button
                type="button"
                className="btn btn-outline"
                style={{ marginTop: '14px' }}
                onClick={onClearFilters}
              >
                Clear All Filters
              </button>
            </>
          ) : (
            <>
              <p className="empty-title">No problems tracked yet!</p>
              <p className="empty-desc">
                Use the "Add New Problem" form above to start tracking your DSA journey.
              </p>
            </>
          )}
        </div>
      ) : (
        /* Table of Problems */
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

                        {problem.notes && (
                          <p
                            className="problem-notes-preview"
                            title={problem.notes}
                          >
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
                        {/* Solve only shows for unsolved problems */}
                        {!isSolved && (
                          <button
                            type="button"
                            className="btn-action btn-solve"
                            title="Mark problem as Solved"
                            onClick={() => onMarkSolved(problem.id)}
                          >
                            Solve
                          </button>
                        )}

                        {/* Revise button */}
                        <button
                          type="button"
                          className="btn-action btn-revise"
                          title="Mark problem as revised"
                          onClick={() => onMarkRevised(problem.id)}
                        >
                          Revise
                        </button>

                        {/* Delete button (with confirmation) */}
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
      )}
    </div>
  );
}
