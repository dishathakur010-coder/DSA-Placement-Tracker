import { TOPICS, DIFFICULTIES, STATUSES } from '../constants';

/**
 * FilterBar Component
 * 
 * What it does:
 * - Provides dropdown filters for Topic, Difficulty, and Status.
 * - Updates the query parameters sent to GET /api/problems.
 * - Includes a "Clear Filters" button to reset all filters.
 * 
 * What props are used:
 * - filters: An object containing { topic, difficulty, status }.
 * - onFilterChange: Callback function triggered when any dropdown value changes.
 * - onClear: Callback function to reset all filters back to empty.
 */
export default function FilterBar({ filters, onFilterChange, onClear }) {
  const handleChange = (field, value) => {
    onFilterChange({
      ...filters,
      [field]: value,
    });
  };

  const hasActiveFilters = Boolean(
    filters.topic || filters.difficulty || filters.status
  );

  return (
    <div className="filter-bar">
      <div className="filter-group">
        {/* Topic Filter */}
        <div className="filter-item">
          <label htmlFor="filter-topic">Filter by Topic:</label>
          <select
            id="filter-topic"
            value={filters.topic || ''}
            onChange={(e) => handleChange('topic', e.target.value)}
          >
            <option value="">All Topics</option>
            {TOPICS.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </div>

        {/* Difficulty Filter */}
        <div className="filter-item">
          <label htmlFor="filter-difficulty">Filter by Difficulty:</label>
          <select
            id="filter-difficulty"
            value={filters.difficulty || ''}
            onChange={(e) => handleChange('difficulty', e.target.value)}
          >
            <option value="">All Difficulties</option>
            {DIFFICULTIES.map((d) => (
              <option key={d} value={d}>
                {d}
              </option>
            ))}
          </select>
        </div>

        {/* Status Filter */}
        <div className="filter-item">
          <label htmlFor="filter-status">Filter by Status:</label>
          <select
            id="filter-status"
            value={filters.status || ''}
            onChange={(e) => handleChange('status', e.target.value)}
          >
            <option value="">All Statuses</option>
            {STATUSES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>

        {/* Clear Filters Button */}
        {hasActiveFilters && (
          <div className="filter-item filter-action">
            <button
              type="button"
              className="btn btn-outline btn-clear"
              onClick={onClear}
              title="Reset all filters"
            >
              ✕ Clear Filters
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

