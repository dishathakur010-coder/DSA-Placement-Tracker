/**
 * RevisionStats Component
 * 
 * Displays:
 * 1) Overall revision statistics (total revisions, mastered count, 7-day revisions, etc.)
 * 2) Strongest and weakest topic callouts (ignoring topics with < 2 problems)
 * 3) A colored strength progress bar for each topic based on score:
 *      - STRONG (>= 70): Green
 *      - MEDIUM (40-69): Orange
 *      - WEAK (< 40): Red
 */
export default function RevisionStats({ stats, loading }) {
  if (loading) {
    return (
      <div className="card stats-card">
        <div className="card-header">
          <h3>Revision Statistics</h3>
          <p className="subtitle">Overall spaced revision metrics and per-topic strength.</p>
        </div>
        <div className="empty-state">
          <p className="loading-text">Loading revision metrics...</p>
        </div>
      </div>
    );
  }

  if (!stats) {
    return null;
  }

  const topicStrengths = stats.topicStrengths || [];

  return (
    <div className="card stats-card revision-stats-card">
      <div className="card-header">
        <div className="header-with-badge">
          <h3>Revision Statistics</h3>
          <span className="count-badge count-badge-success">
            {stats.totalRevisionsDone} total revisions
          </span>
        </div>
        <p className="subtitle">
          Track revision volume, problem mastery, and retention strength across topics.
        </p>
      </div>

      {/* Summary Stat Boxes */}
      <div className="stats-summary-grid">
        <div className="stat-box">
          <span className="stat-label">Total Revisions</span>
          <span className="stat-value text-primary">{stats.totalRevisionsDone}</span>
          <span className="stat-hint">{stats.averageRevisionsPerProblem} avg / problem</span>
        </div>

        <div className="stat-box">
          <span className="stat-label">Mastered Problems</span>
          <span className="stat-value text-success">{stats.problemsMastered}</span>
          <span className="stat-hint">Revised 3+ times</span>
        </div>

        <div className="stat-box">
          <span className="stat-label">Revised (1+ times)</span>
          <span className="stat-value">{stats.problemsRevisedAtLeastOnce}</span>
          <span className="stat-hint">{stats.problemsNeverRevised} never revised</span>
        </div>

        <div className="stat-box">
          <span className="stat-label">Past 7 Days</span>
          <span className="stat-value text-primary">{stats.revisionsLast7Days}</span>
          <span className="stat-hint">Recent activity</span>
        </div>
      </div>

      {/* Strongest & Weakest Callouts */}
      {(stats.strongestTopic || stats.weakestTopic) && (
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginTop: '16px' }}>
          {stats.strongestTopic && (
            <div
              style={{
                flex: '1',
                minWidth: '220px',
                padding: '10px 14px',
                backgroundColor: '#ecfdf5',
                border: '1px solid #a7f3d0',
                borderRadius: '8px',
                fontSize: '13px',
                color: '#065f46',
              }}
            >
              🏆 <strong>Strongest Topic:</strong> {stats.strongestTopic}
            </div>
          )}

          {stats.weakestTopic && (
            <div
              style={{
                flex: '1',
                minWidth: '220px',
                padding: '10px 14px',
                backgroundColor: '#fff7ed',
                border: '1px solid #fed7aa',
                borderRadius: '8px',
                fontSize: '13px',
                color: '#9a3412',
              }}
            >
              ⚠️ <strong>Weakest Topic:</strong> {stats.weakestTopic} (needs review)
            </div>
          )}
        </div>
      )}

      {/* Per-Topic Strength Bars */}
      {topicStrengths.length > 0 && (
        <div className="stats-section" style={{ marginTop: '20px' }}>
          <h4 className="stats-section-title">Topic Strength Scores (0 - 100)</h4>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '12px' }}>
            {topicStrengths.map((item) => {
              const label = item.strengthLabel || 'WEAK';
              const score = item.strengthScore || 0;
              let fillGradient = 'linear-gradient(90deg, #ef4444, #dc2626)'; // Red for WEAK
              let badgeClass = 'badge badge-weak';

              if (label === 'STRONG') {
                fillGradient = 'linear-gradient(90deg, #22c55e, #16a34a)'; // Green
                badgeClass = 'badge badge-strong';
              } else if (label === 'MEDIUM') {
                fillGradient = 'linear-gradient(90deg, #f59e0b, #d97706)'; // Orange
                badgeClass = 'badge badge-medium';
              }

              return (
                <div key={item.topic} className="topic-bar-item">
                  <div className="topic-bar-info">
                    <span className="topic-bar-name">
                      {item.topic}
                      <span className={badgeClass}>{label}</span>
                    </span>
                    <span className="topic-bar-numbers">
                      <strong>{score.toFixed(1)}</strong> / 100 ({item.totalSolved} solved)
                    </span>
                  </div>

                  <div className="progress-track" style={{ marginTop: '6px' }}>
                    <div
                      className="progress-fill"
                      style={{
                        width: `${Math.min(100, Math.max(0, score))}%`,
                        background: fillGradient,
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

