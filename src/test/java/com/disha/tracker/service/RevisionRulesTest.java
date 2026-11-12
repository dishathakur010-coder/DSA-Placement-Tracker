package com.disha.tracker.service;

import com.disha.tracker.entity.Problem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevisionRulesTest {

    @Test
    @DisplayName("Never revised problem: canRevise is true, daysUntilRevisable is 0, mastered is false")
    void testNeverRevisedProblem() {
        LocalDate today = LocalDate.now();
        Problem problem = new Problem("Two Sum", "Arrays", "Easy", "link", "Solved", "notes");
        problem.setRevisionCount(0);
        problem.setSolvedDate(today.minusDays(10));
        problem.setLastRevisedDate(null);

        assertTrue(RevisionRules.canRevise(problem, today));
        assertEquals(0L, RevisionRules.daysUntilRevisable(problem, today));
        assertFalse(RevisionRules.isMastered(problem));
    }

    @Test
    @DisplayName("6 days after 1st revision: locked (canRevise=false, daysUntilRevisable=1)")
    void testSixDaysAfterFirstRevision() {
        LocalDate today = LocalDate.now();
        Problem problem = new Problem("Reverse Linked List", "Linked List", "Easy", "link", "Solved", "notes");
        problem.setRevisionCount(1);
        problem.setSolvedDate(today.minusDays(20));
        problem.setLastRevisedDate(today.minusDays(6));

        assertFalse(RevisionRules.canRevise(problem, today));
        assertEquals(1L, RevisionRules.daysUntilRevisable(problem, today));
        assertFalse(RevisionRules.isMastered(problem));
    }

    @Test
    @DisplayName("7 days after 1st revision: unlocked (canRevise=true, daysUntilRevisable=0)")
    void testSevenDaysAfterFirstRevision() {
        LocalDate today = LocalDate.now();
        Problem problem = new Problem("Reverse Linked List", "Linked List", "Easy", "link", "Solved", "notes");
        problem.setRevisionCount(1);
        problem.setSolvedDate(today.minusDays(20));
        problem.setLastRevisedDate(today.minusDays(7));

        assertTrue(RevisionRules.canRevise(problem, today));
        assertEquals(0L, RevisionRules.daysUntilRevisable(problem, today));
        assertFalse(RevisionRules.isMastered(problem));
    }

    @Test
    @DisplayName("13 days after 2nd revision: locked (canRevise=false, daysUntilRevisable=1)")
    void testThirteenDaysAfterSecondRevision() {
        LocalDate today = LocalDate.now();
        Problem problem = new Problem("LRU Cache", "Design", "Medium", "link", "Solved", "notes");
        problem.setRevisionCount(2);
        problem.setSolvedDate(today.minusDays(30));
        problem.setLastRevisedDate(today.minusDays(13));

        assertFalse(RevisionRules.canRevise(problem, today));
        assertEquals(1L, RevisionRules.daysUntilRevisable(problem, today));
        assertFalse(RevisionRules.isMastered(problem));
    }

    @Test
    @DisplayName("14 days after 2nd revision: unlocked (canRevise=true, daysUntilRevisable=0)")
    void testFourteenDaysAfterSecondRevision() {
        LocalDate today = LocalDate.now();
        Problem problem = new Problem("LRU Cache", "Design", "Medium", "link", "Solved", "notes");
        problem.setRevisionCount(2);
        problem.setSolvedDate(today.minusDays(30));
        problem.setLastRevisedDate(today.minusDays(14));

        assertTrue(RevisionRules.canRevise(problem, today));
        assertEquals(0L, RevisionRules.daysUntilRevisable(problem, today));
        assertFalse(RevisionRules.isMastered(problem));
    }

    @Test
    @DisplayName("Mastered rule: 3 or more revisions marks problem mastered and locked")
    void testMasteredProblem() {
        LocalDate today = LocalDate.now();
        Problem problem = new Problem("Merge K Sorted Lists", "Heap", "Hard", "link", "Solved", "notes");
        problem.setRevisionCount(3);
        problem.setSolvedDate(today.minusDays(40));
        problem.setLastRevisedDate(today.minusDays(20));

        assertTrue(RevisionRules.isMastered(problem));
        assertFalse(RevisionRules.canRevise(problem, today));
        assertEquals(0L, RevisionRules.daysUntilRevisable(problem, today));
    }

    @Test
    @DisplayName("Topic strength score calculation and label thresholds")
    void testStrengthScoreAndLabels() {
        // Perfect score: 100% revised, average 3 revisions, activity today (0 days)
        // Score = 50 * 1 + 30 * 1 + 20 * 1 = 100
        double perfect = RevisionRules.calculateStrengthScore(10, 10, 3.0, 0);
        assertEquals(100.0, perfect);
        assertEquals("STRONG", RevisionRules.getStrengthLabel(perfect));

        // Strong threshold boundary: 70.0
        assertEquals("STRONG", RevisionRules.getStrengthLabel(70.0));
        assertEquals("STRONG", RevisionRules.getStrengthLabel(85.5));

        // Medium threshold boundary: 40.0 to 69.9
        assertEquals("MEDIUM", RevisionRules.getStrengthLabel(69.9));
        assertEquals("MEDIUM", RevisionRules.getStrengthLabel(40.0));

        // Weak: < 40.0
        assertEquals("WEAK", RevisionRules.getStrengthLabel(39.9));
        assertEquals("WEAK", RevisionRules.getStrengthLabel(15.0));
        assertEquals("WEAK", RevisionRules.getStrengthLabel(0.0));
    }
}

