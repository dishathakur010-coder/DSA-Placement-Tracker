/**
 * TopicInsights Component
 * 
 * What it does:
 * - Displays topic-level preparation and revision freshness metrics.
 * - Shows total solved, solved in last 30 days, last solved date, and days since last activity.
 * - Shows a colored status badge:
 *     - FRESH (<=7 days): Green
 *     - DUE_SOON (8-14 days): Orange
 *     - OVERDUE (>14 days): Red
 * - Displays highlights for "Most neglected" (highest pending days with >=3 solved) and "Strongest topic".
 * - Automatically updates after syncing, solving, or revising problems.
 */
export default function TopicInsights({ insights, loading }) {
  const topics = insights?.topics || [];
  const mostNeglectedTopic = insights?.mostNeglectedTopic;
  const mostNeglectedDays = insights?.mostNeglectedDays;
  const strongestTopic = insights?.strongestTopic;
  const strongestTopicSolved = insights?.strongestTopicSolved;

  return (
    <div className="card topic-insights-card">
      <div className="card-header">
        <div className="header-with-badge">
          <h3>Topic Insights</h3>
          {!loading && topics.length > 0 && (
            <span className="count-badge">
              {topics.length} {topics.length === 1 ? 'topic' : 'topics'}
            </span>
          )}
        </div>
        <p className="subtitle">
          Track revision recency per topic and identify topics at risk of being forgotten.
        </p>
      </div>

      {/* Highlights Banner */}
      {!loading && (mostNeglectedTopic || strongestTopic) && (
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginBottom: '16px' }}>
          {mostNeglectedTopic && (
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 14px',
                backgroundColor: '#fff7ed',
                border: '1px solid #fed7aa',
                borderRadius: '8px',
                fontSize: '13px',
                color: '#9a3412',
                fontWeight: '500',
              }}
            >
              <span>⚠️</span>
              <span>
                <strong>Most neglected:</strong> {mostNeglectedTopic} ({mostNeglectedDays} {mostNeglectedDays === 1 ? 'day' : 'days'} ago)
              </span>
            </div>
          )}

          {strongestTopic && (
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 14px',
                backgroundColor: '#ecfdf5',
                border: '1px solid #a7f3d0',
                borderRadius: '8px',
                fontSize: '13px',
                color: '#065f46',
                fontWeight: '500',
              }}
            >
              <span>🏆</span>
              <span>
                <strong>Strongest topic:</strong> {strongestTopic} ({strongestTopicSolved} solved)
              </span>
            </div>
          )}
        </div>
      )}

      {loading ? (
        <div className="empty-state">
          <p className="loading-text">Analyzing topic revision history...</p>
        </div>
      ) : topics.length === 0 ? (
        <div className="empty-state">
          <p className="empty-title">No topic insights yet</p>
          <p className="empty-description">
            Solve problems or sync your Codeforces profile to see topic revision recency and neglected topics!
          </p>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="problems-table">
            <thead>
              <tr>
                <th>Topic</th>
                <th>Solved Count</th>
                <th>Last Solved Date</th>
                <th>Activity Recency</th>
                <th>Revision Status</th>
              </tr>
            </thead>
            <tbody>
              {topics.map((t) => {
                const days = t.daysSinceLastActivity;
                let statusLabel = 'Fresh';
                let statusBadgeClass = 'badge badge-fresh';

                if (t.revisionStatus === 'OVERDUE') {
                  statusLabel = `Overdue (${days}d)`;
                  statusBadgeClass = 'badge badge-overdue';
                } else if (t.revisionStatus === 'DUE_SOON') {
                  statusLabel = `Due Soon (${days}d)`;
                  statusBadgeClass = 'badge badge-due-soon';
                } else {
                  statusLabel = `Fresh (${days}d)`;
                  statusBadgeClass = 'badge badge-fresh';
                }

                return (
                  <tr key={t.topic}>
                    <td>
                      <span className="topic-tag" style={{ fontWeight: '600' }}>
                        {t.topic}
                      </span>
                    </td>
                    <td>
                      <strong>{t.totalSolved}</strong>
                      {t.solvedLast30Days > 0 && (
                        <span style={{ fontSize: '12px', color: '#16a34a', marginLeft: '6px' }}>
                          (+{t.solvedLast30Days} in 30d)
                        </span>
                      )}
                    </td>
                    <td>{t.lastSolvedDate || '—'}</td>
                    <td>
                      {t.daysSinceLastActivity === 0
                        ? 'Today'
                        : `${t.daysSinceLastActivity} ${t.daysSinceLastActivity === 1 ? 'day' : 'days'} ago`}
                    </td>
                    <td>
                      <span className={statusBadgeClass}>{statusLabel}</span>
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

