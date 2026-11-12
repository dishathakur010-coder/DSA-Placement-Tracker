/**
 * TopicCards Component
 * 
 * Displays a responsive grid of cards for each topic the user has solved problems in:
 * - Topic name
 * - "X days since last activity" in large text
 * - Solved count, revised count, mastered count
 * - Suggested problem ("Suggested: <title>" or "Nothing to revise right now")
 * - Colored strength badge (STRONG, MEDIUM, WEAK)
 * - Clicking a card selects that topic to open its problem list.
 */
export default function TopicCards({ topics, loading, onSelectTopic }) {
  if (loading) {
    return (
      <div className="card topic-cards-panel">
        <div className="card-header">
          <h3>Topic Revision Cards</h3>
          <p className="subtitle">Track your revision recency and suggested problems per topic.</p>
        </div>
        <div className="empty-state">
          <p className="loading-text">Loading topic cards...</p>
        </div>
      </div>
    );
  }

  if (!topics || topics.length === 0) {
    return (
      <div className="card topic-cards-panel">
        <div className="card-header">
          <h3>Topic Revision Cards</h3>
          <p className="subtitle">Track your revision recency and suggested problems per topic.</p>
        </div>
        <div className="empty-state">
          <p className="empty-title">No solved problems yet</p>
          <p className="empty-description">
            Solve problems or sync your Codeforces profile to populate your topic revision cards!
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="card topic-cards-panel">
      <div className="card-header">
        <div className="header-with-badge">
          <h3>Topic Revision Cards</h3>
          <span className="count-badge">{topics.length} {topics.length === 1 ? 'topic' : 'topics'}</span>
        </div>
        <p className="subtitle">
          Click any card to inspect problems, view the recommended next problem, or revise.
        </p>
      </div>

      <div className="topic-grid">
        {topics.map((t) => {
          const daysText = t.daysSinceLastActivity === 0
            ? 'Today'
            : `${t.daysSinceLastActivity}d`;

          const subtitleDays = t.daysSinceLastActivity === 0
            ? 'Active today'
            : `${t.daysSinceLastActivity} ${t.daysSinceLastActivity === 1 ? 'day' : 'days'} since last activity`;

          const strengthBadgeClass = `badge badge-${(t.strengthLabel || 'weak').toLowerCase()}`;

          return (
            <div
              key={t.topic}
              className="topic-card"
              onClick={() => onSelectTopic(t.topic)}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  onSelectTopic(t.topic);
                }
              }}
              title={`Click to view problems in ${t.topic}`}
            >
              <div className="topic-card-top">
                <h4 className="topic-card-title">{t.topic}</h4>
                <span className={strengthBadgeClass}>
                  {t.strengthLabel} ({t.strengthScore})
                </span>
              </div>

              {/* Large Days Text */}
              <div className="topic-card-activity">
                <span className="topic-activity-number">{daysText}</span>
                <span className="topic-activity-label">{subtitleDays}</span>
              </div>

              {/* Counts Grid */}
              <div className="topic-card-stats">
                <div className="topic-stat-item">
                  <span className="topic-stat-num">{t.totalSolved}</span>
                  <span className="topic-stat-lbl">Solved</span>
                </div>
                <div className="topic-stat-item">
                  <span className="topic-stat-num">{t.revisedCount}</span>
                  <span className="topic-stat-lbl">Revised</span>
                </div>
                <div className="topic-stat-item">
                  <span className="topic-stat-num">{t.masteredCount}</span>
                  <span className="topic-stat-lbl">Mastered</span>
                </div>
              </div>

              {/* Suggested Problem */}
              <div className="topic-card-suggested">
                {t.suggestedProblem ? (
                  <>
                    <span className="suggested-icon">🎯 Suggested:</span>
                    <span className="suggested-title" title={t.suggestedProblem.title}>
                      {t.suggestedProblem.title}
                    </span>
                  </>
                ) : (
                  <span className="suggested-empty">✓ Nothing to revise right now</span>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

