package com.disha.tracker.service;

import com.disha.tracker.entity.Problem;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Encapsulates the revision rules, lock durations, mastery thresholds,
 * and topic strength calculations for the DSA Tracker.
 */
public final class RevisionRules {

    // Lock periods (in days)
    public static final int REVISION_LOCK_AFTER_FIRST_DAYS = 7;
    public static final int REVISION_LOCK_AFTER_SECOND_DAYS = 14;
    public static final int MASTERED_REVISION_COUNT = 3;

    // Topic strength weights
    public static final double WEIGHT_REVISED_RATIO = 50.0;
    public static final double WEIGHT_AVG_REVISIONS = 30.0;
    public static final double WEIGHT_RECENCY = 20.0;

    // Strength score thresholds
    public static final double STRONG_THRESHOLD = 70.0;
    public static final double MEDIUM_THRESHOLD = 40.0;

    private RevisionRules() {
        // Prevent instantiation
    }

    /**
     * A problem is mastered once it has been revised 3 times.
     */
    public static boolean isMastered(Problem problem) {
        return problem != null && problem.getRevisionCount() >= MASTERED_REVISION_COUNT;
    }

    /**
     * Checks if a problem can be revised today.
     * Rules:
     * - Must be marked as Solved and not mastered.
     * - 0 revisions: immediately revisable.
     * - 1 revision: locked for 7 days after the first revision.
     * - 2 revisions: locked for 14 days after the second revision.
     * - 3+ revisions: mastered, cannot be revised further.
     */
    public static boolean canRevise(Problem problem, LocalDate today) {
        if (problem == null || isMastered(problem)) {
            return false;
        }
        if (problem.getStatus() == null || !"Solved".equalsIgnoreCase(problem.getStatus())) {
            return false;
        }

        int count = problem.getRevisionCount();
        if (count == 0) {
            return true;
        }

        LocalDate baseDate = (problem.getLastRevisedDate() != null)
                ? problem.getLastRevisedDate()
                : problem.getSolvedDate();

        if (baseDate == null) {
            return true;
        }

        int lockDays = (count == 1)
                ? REVISION_LOCK_AFTER_FIRST_DAYS
                : REVISION_LOCK_AFTER_SECOND_DAYS;

        LocalDate availableDate = baseDate.plusDays(lockDays);
        return !today.isBefore(availableDate);
    }

    /**
     * Returns the remaining days until the problem can be revised again.
     * Returns 0 if already revisable, mastered, or never revised.
     */
    public static long daysUntilRevisable(Problem problem, LocalDate today) {
        if (problem == null || isMastered(problem) || canRevise(problem, today)) {
            return 0L;
        }

        int count = problem.getRevisionCount();
        if (count == 0) {
            return 0L;
        }

        LocalDate baseDate = (problem.getLastRevisedDate() != null)
                ? problem.getLastRevisedDate()
                : problem.getSolvedDate();

        if (baseDate == null) {
            return 0L;
        }

        int lockDays = (count == 1)
                ? REVISION_LOCK_AFTER_FIRST_DAYS
                : REVISION_LOCK_AFTER_SECOND_DAYS;

        LocalDate availableDate = baseDate.plusDays(lockDays);
        long days = ChronoUnit.DAYS.between(today, availableDate);
        return Math.max(0L, days);
    }

    /**
     * Returns the exact calendar date on which the problem will unlock.
     */
    public static LocalDate getAvailableDate(Problem problem) {
        if (problem == null || isMastered(problem)) {
            return null;
        }

        int count = problem.getRevisionCount();
        if (count == 0) {
            return LocalDate.now();
        }

        LocalDate baseDate = (problem.getLastRevisedDate() != null)
                ? problem.getLastRevisedDate()
                : problem.getSolvedDate();

        if (baseDate == null) {
            return LocalDate.now();
        }

        int lockDays = (count == 1)
                ? REVISION_LOCK_AFTER_FIRST_DAYS
                : REVISION_LOCK_AFTER_SECOND_DAYS;

        return baseDate.plusDays(lockDays);
    }

    /**
     * Calculate strength score (0 to 100) for a topic.
     * Score = 50 * (revisedCount / totalSolved)
     *       + 30 * min(averageRevisionCount / 3, 1)
     *       + 20 * (1 - min(daysSinceLastActivity / 30, 1))
     */
    public static double calculateStrengthScore(long totalSolved, long revisedCount,
                                                double averageRevisionCount, long daysSinceLastActivity) {
        if (totalSolved <= 0) {
            return 0.0;
        }

        double term1 = WEIGHT_REVISED_RATIO * ((double) revisedCount / totalSolved);
        double term2 = WEIGHT_AVG_REVISIONS * Math.min(averageRevisionCount / (double) MASTERED_REVISION_COUNT, 1.0);
        double term3 = WEIGHT_RECENCY * (1.0 - Math.min((double) daysSinceLastActivity / 30.0, 1.0));

        double raw = term1 + term2 + term3;
        double clamped = Math.max(0.0, Math.min(100.0, raw));
        return Math.round(clamped * 10.0) / 10.0;
    }

    /**
     * Determine strength label based on score:
     * - STRONG (>= 70)
     * - MEDIUM (40 to 69)
     * - WEAK (< 40)
     */
    public static String getStrengthLabel(double score) {
        if (score >= STRONG_THRESHOLD) {
            return "STRONG";
        }
        if (score >= MEDIUM_THRESHOLD) {
            return "MEDIUM";
        }
        return "WEAK";
    }
}

