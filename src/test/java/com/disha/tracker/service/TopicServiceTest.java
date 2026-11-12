package com.disha.tracker.service;

import com.disha.tracker.dto.ProblemResponse;
import com.disha.tracker.dto.RevisionStatsResponse;
import com.disha.tracker.dto.TopicSummaryDto;
import com.disha.tracker.entity.Problem;
import com.disha.tracker.entity.User;
import com.disha.tracker.repository.ProblemRepository;
import com.disha.tracker.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TopicService topicService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Disha");
        testUser.setEmail("disha@example.com");

        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getName()).thenReturn("disha@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(userRepository.findByEmail("disha@example.com")).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Suggested problem order: 1) never revised, oldest solvedDate first; 2) lowest revisionCount, oldest lastRevisedDate")
    void shouldOrderSuggestedProblemCorrectly() {
        LocalDate today = LocalDate.now();

        // Problem A: never revised, solved 25 days ago
        Problem probA = new Problem("Problem A", "Arrays", "Easy", "link", "Solved", "notes");
        probA.setId(1L);
        probA.setRevisionCount(0);
        probA.setSolvedDate(today.minusDays(25));
        probA.setLastRevisedDate(null);

        // Problem B: never revised, solved 10 days ago
        Problem probB = new Problem("Problem B", "Arrays", "Easy", "link", "Solved", "notes");
        probB.setId(2L);
        probB.setRevisionCount(0);
        probB.setSolvedDate(today.minusDays(10));
        probB.setLastRevisedDate(null);

        // Problem C: revised 1 time, last revised 10 days ago (unlocked because 10 >= 7)
        Problem probC = new Problem("Problem C", "Arrays", "Medium", "link", "Solved", "notes");
        probC.setId(3L);
        probC.setRevisionCount(1);
        probC.setSolvedDate(today.minusDays(30));
        probC.setLastRevisedDate(today.minusDays(10));

        // Problem D: revised 2 times, last revised 20 days ago (unlocked because 20 >= 14)
        Problem probD = new Problem("Problem D", "Arrays", "Hard", "link", "Solved", "notes");
        probD.setId(4L);
        probD.setRevisionCount(2);
        probD.setSolvedDate(today.minusDays(40));
        probD.setLastRevisedDate(today.minusDays(20));

        List<Problem> list = List.of(probC, probD, probB, probA);
        List<Problem> sorted = list.stream().sorted(TopicService.SUGGESTED_COMPARATOR).toList();

        // Expected order: probA (oldest never revised), probB (newer never revised), probC (revisionCount 1), probD (revisionCount 2)
        assertEquals(1L, sorted.get(0).getId());
        assertEquals(2L, sorted.get(1).getId());
        assertEquals(3L, sorted.get(2).getId());
        assertEquals(4L, sorted.get(3).getId());
    }

    @Test
    @DisplayName("Topic problem list orders: suggested first, revised DESC, never-revised ASC, mastered last")
    void shouldOrderTopicProblemsAccordingToSpecification() {
        LocalDate today = LocalDate.now();

        // 1. Suggested problem (never revised, oldest solved: 20 days ago)
        Problem pSuggested = new Problem("Suggested One", "Trees", "Easy", "link", "Solved", "notes");
        pSuggested.setId(10L);
        pSuggested.setRevisionCount(0);
        pSuggested.setSolvedDate(today.minusDays(20));

        // 2. Never revised but newer solve (10 days ago)
        Problem pNeverRevised = new Problem("Never Revised", "Trees", "Easy", "link", "Solved", "notes");
        pNeverRevised.setId(11L);
        pNeverRevised.setRevisionCount(0);
        pNeverRevised.setSolvedDate(today.minusDays(10));

        // 3. Revised problems:
        // pRevRecent: revised 1 day ago (locked)
        Problem pRevRecent = new Problem("Revised Recent", "Trees", "Medium", "link", "Solved", "notes");
        pRevRecent.setId(12L);
        pRevRecent.setRevisionCount(1);
        pRevRecent.setSolvedDate(today.minusDays(25));
        pRevRecent.setLastRevisedDate(today.minusDays(1));

        // pRevOlder: revised 8 days ago (unlocked)
        Problem pRevOlder = new Problem("Revised Older", "Trees", "Medium", "link", "Solved", "notes");
        pRevOlder.setId(13L);
        pRevOlder.setRevisionCount(1);
        pRevOlder.setSolvedDate(today.minusDays(30));
        pRevOlder.setLastRevisedDate(today.minusDays(8));

        // 4. Mastered problem
        Problem pMastered = new Problem("Mastered Problem", "Trees", "Hard", "link", "Solved", "notes");
        pMastered.setId(14L);
        pMastered.setRevisionCount(3);
        pMastered.setSolvedDate(today.minusDays(50));
        pMastered.setLastRevisedDate(today.minusDays(15));

        when(problemRepository.findByUserId(testUser.getId())).thenReturn(List.of(
                pMastered, pRevOlder, pNeverRevised, pRevRecent, pSuggested
        ));

        List<ProblemResponse> result = topicService.getTopicProblems("Trees");

        assertEquals(5, result.size());

        // 1st: Suggested problem (highlighted)
        assertEquals(10L, result.get(0).getId());
        assertTrue(result.get(0).isSuggested());

        // 2nd and 3rd: Revised problems sorted by lastRevisedDate DESC (1 day ago before 8 days ago)
        assertEquals(12L, result.get(1).getId());
        assertEquals(13L, result.get(2).getId());

        // 4th: Never revised (oldest solved first)
        assertEquals(11L, result.get(3).getId());

        // 5th: Mastered problem placed last
        assertEquals(14L, result.get(4).getId());
        assertTrue(result.get(4).isMastered());
    }

    @Test
    @DisplayName("getTopicsSummary calculates daysSinceLastActivity and sorts by most overdue first")
    void shouldCalculateDaysSinceLastActivityAndSortByHighestOverdueFirst() {
        LocalDate today = LocalDate.now();

        // Arrays: last activity 5 days ago (solved 30 days ago, revised 5 days ago)
        Problem arr = new Problem("Two Sum", "Arrays", "Easy", "link", "Solved", "notes");
        arr.setId(1L);
        arr.setSolvedDate(today.minusDays(30));
        arr.setLastRevisedDate(today.minusDays(5));

        // Trees: last activity 40 days ago (solved 40 days ago, never revised)
        Problem tree = new Problem("Invert Tree", "Trees", "Easy", "link", "Solved", "notes");
        tree.setId(2L);
        tree.setSolvedDate(today.minusDays(40));
        tree.setLastRevisedDate(null);

        // DP: last activity 15 days ago
        Problem dp = new Problem("Climbing Stairs", "Dynamic Programming", "Easy", "link", "Solved", "notes");
        dp.setId(3L);
        dp.setSolvedDate(today.minusDays(15));
        dp.setLastRevisedDate(null);

        when(problemRepository.findByUserId(testUser.getId())).thenReturn(List.of(arr, tree, dp));

        List<TopicSummaryDto> summaries = topicService.getTopicsSummary();

        assertEquals(3, summaries.size());
        // Highest daysSinceLastActivity first: Trees (40d) -> DP (15d) -> Arrays (5d)
        assertEquals("Trees", summaries.get(0).getTopic());
        assertEquals(40L, summaries.get(0).getDaysSinceLastActivity());

        assertEquals("Dynamic Programming", summaries.get(1).getTopic());
        assertEquals(15L, summaries.get(1).getDaysSinceLastActivity());

        assertEquals("Arrays", summaries.get(2).getTopic());
        assertEquals(5L, summaries.get(2).getDaysSinceLastActivity());
    }

    @Test
    @DisplayName("getRevisionStats calculates summary numbers and ignores topics with fewer than 2 problems for strongest/weakest")
    void shouldCalculateRevisionStatsAndIgnoreSmallTopicsForStrongestWeakest() {
        LocalDate today = LocalDate.now();

        // DP: 1 problem, 100 strength -> Must be IGNORED for strongest/weakest (< 2 problems)
        Problem dp1 = new Problem("DP 1", "DP", "Easy", "link", "Solved", "notes");
        dp1.setId(1L);
        dp1.setRevisionCount(3); // Mastered
        dp1.setSolvedDate(today);
        dp1.setLastRevisedDate(today);

        // Arrays: 2 problems (both mastered) -> High strength, eligible!
        Problem a1 = new Problem("A 1", "Arrays", "Easy", "link", "Solved", "notes");
        a1.setId(2L);
        a1.setRevisionCount(3);
        a1.setSolvedDate(today);
        a1.setLastRevisedDate(today);
        Problem a2 = new Problem("A 2", "Arrays", "Easy", "link", "Solved", "notes");
        a2.setId(3L);
        a2.setRevisionCount(3);
        a2.setSolvedDate(today);
        a2.setLastRevisedDate(today);

        // Trees: 2 problems (0 revisions, solved 60 days ago) -> Low strength, eligible!
        Problem t1 = new Problem("T 1", "Trees", "Easy", "link", "Solved", "notes");
        t1.setId(4L);
        t1.setRevisionCount(0);
        t1.setSolvedDate(today.minusDays(60));
        Problem t2 = new Problem("T 2", "Trees", "Easy", "link", "Solved", "notes");
        t2.setId(5L);
        t2.setRevisionCount(0);
        t2.setSolvedDate(today.minusDays(60));

        when(problemRepository.findByUserId(testUser.getId())).thenReturn(List.of(dp1, a1, a2, t1, t2));

        RevisionStatsResponse stats = topicService.getRevisionStats();

        assertNotNull(stats);
        assertEquals(9L, stats.getTotalRevisionsDone()); // 3 + 3 + 3
        assertEquals(3L, stats.getProblemsMastered());    // dp1, a1, a2
        assertEquals(2L, stats.getProblemsNeverRevised()); // t1, t2

        // Strongest topic is Arrays, ignoring DP
        assertEquals("Arrays", stats.getStrongestTopic());
        // Weakest topic is Trees
        assertEquals("Trees", stats.getWeakestTopic());
    }
}

