/**
 * StatsPanel Component
 * 
 * What it does:
 * - Displays DSA preparation statistics returned by GET /api/stats:
 *     - Summary counts: Total, Solved, and Pending problems.
 *     - Weakest Topic: Highlighted based on backend calculation (min 2 problems).
 *     - Difficulty Breakdown: Easy, Medium, and Hard counts (solved / total).
 *     - Topic Breakdown: Clean visual progress bars showing solvedPercentage for each topic.
 * 
 * What props are used:
 * - stats: StatsResponse object from backend.
 * - loading: Whether stats are currently loading.
 */
export default function StatsPanel({ stats, loading }) {
  if (loading) {
    return (
      <div className="card stats-card">
        <div className="card-header">
          <h3>Preparation Statistics</h3>
        </div>
        <div className="empty-state">
          <p className="loading-text">Loading your stats...</p>
        </div>
      </div>
    );
  }

  if (!stats || stats.totalProblems === 0) {
    return (
      <div className="card stats-card">
        <div className="card-header">
          <h3>Preparation Statistics</h3>
        </div>
        <div className="empty-state">
          <p className="empty-title">No statistics available yet</p>
          <p className="empty-desc">
            Add and solve problems to see your topic breakdown, difficulty mastery, and weakest topic.
          </p>
        </div>
      </div>
    );
  }

  const {
    totalProblems = 0,
    solvedCount = 0,
    pendingCount = 0,
    topicBreakdown = [],
    difficultyBreakdown = {},
    weakestTopic = null,
  } = stats;

  const easyStats = difficultyBreakdown?.Easy || { total: 0, solved: 0 };
  const mediumStats = difficultyBreakdown?.Medium || { total: 0, solved: 0 };
  const hardStats = difficultyBreakdown?.Hard || { total: 0, solved: 0 };

  // Calculate overall solved percentage
  const overallPercentage =
    totalProblems > 0
      ? Math.round((solvedCount / totalProblems) * 100)
      : 0;

  return (
    <div className="card stats-card">
      <div className="card-header">
        <div className="header-with-badge">
          <h3>Preparation Statistics</h3>
          <span className="count-badge">{overallPercentage}% overall</span>
        </div>
        <p className="subtitle">
          Real-time metrics on your problem-solving consistency and progress.
        </p>
      </div>

      {/* Summary Metrics Row */}
      <div className="stats-summary-grid">
        <div className="stat-box">
          <span className="stat-label">Total Problems</span>
          <span className="stat-value">{totalProblems}</span>
        </div>

        <div className="stat-box stat-box-solved">
          <span className="stat-label">Solved</span>
          <span className="stat-value text-success">{solvedCount}</span>
        </div>

        <div className="stat-box stat-box-pending">
          <span className="stat-label">Pending</span>
          <span className="stat-value text-warning">{pendingCount}</span>
        </div>

        <div className={`stat-box ${weakestTopic ? 'stat-box-weakest' : ''}`}>
          <span className="stat-label">Weakest Topic</span>
          <span className="stat-value stat-weakest-text">
            {weakestTopic ? weakestTopic : 'None yet'}
          </span>
          {!weakestTopic && (
            <span className="stat-hint">(Needs ≥ 2 problems in a topic)</span>
          )}
        </div>
      </div>

      {/* Difficulty Breakdown */}
      <div className="stats-section">
        <h4 className="stats-section-title">Difficulty Breakdown</h4>
        <div className="difficulty-grid">
          <div className="difficulty-card diff-easy">
            <span className="badge badge-easy">Easy</span>
            <span className="diff-count">
              <strong>{easyStats.solved}</strong> / {easyStats.total} solved
            </span>
          </div>

          <div className="difficulty-card diff-medium">
            <span className="badge badge-medium">Medium</span>
            <span className="diff-count">
              <strong>{mediumStats.solved}</strong> / {mediumStats.total} solved
            </span>
          </div>

          <div className="difficulty-card diff-hard">
            <span className="badge badge-hard">Hard</span>
            <span className="diff-count">
              <strong>{hardStats.solved}</strong> / {hardStats.total} solved
            </span>
          </div>
        </div>
      </div>

      {/* Topic Breakdown with Progress Bars */}
      {topicBreakdown.length > 0 && (
        <div className="stats-section">
          <h4 className="stats-section-title">Topic Breakdown</h4>
          <div className="topic-bars-list">
            {topicBreakdown.map((item) => {
              const isWeakest =
                weakestTopic &&
                weakestTopic.toLowerCase() === item.topic.toLowerCase();

              return (
                <div
                  key={item.topic}
                  className={`topic-bar-row ${
                    isWeakest ? 'topic-bar-weakest' : ''
                  }`}
                >
                  <div className="topic-bar-info">
                    <span className="topic-bar-name">
                      {item.topic}
                      {isWeakest && (
                        <span className="badge badge-weakest-tag">
                          🎯 Weakest Topic
                        </span>
                      )}
                    </span>
                    <span className="topic-bar-numbers">
                      {item.solved} / {item.total} ({item.solvedPercentage}%)
                    </span>
                  </div>

                  <div className="progress-track">
                    <div
                      className={`progress-fill ${
                        isWeakest ? 'progress-weakest' : ''
                      }`}
                      style={{
                        width: `${Math.min(
                          100,
                          Math.max(0, item.solvedPercentage)
                        )}%`,
                      }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

