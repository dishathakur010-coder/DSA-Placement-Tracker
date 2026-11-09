package com.disha.tracker.service;

import com.disha.tracker.dto.ProblemRequest;
import com.disha.tracker.dto.ProblemResponse;
import com.disha.tracker.dto.ProblemRevisionResponse;
import com.disha.tracker.dto.StatsResponse;
import com.disha.tracker.entity.Problem;
import com.disha.tracker.entity.User;
import com.disha.tracker.exception.ResourceNotFoundException;
import com.disha.tracker.repository.ProblemRepository;
import com.disha.tracker.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProblemService problemService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 1. Prepare a simulated test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test Coder");
        testUser.setEmail("coder@example.com");

        // 2. Mock the Spring Security context so ProblemService's getCurrentUser() knows who is logged in
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getName()).thenReturn("coder@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // 3. Mock userRepository returning our testUser whenever queried with coder@example.com
        lenient().when(userRepository.findByEmail("coder@example.com")).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() {
        // Clean up the security context after each test so each test runs in isolation
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // 1. Spaced Revision Interval Logic Tests
    // Formula: 0 -> 1 day, 1 -> 3 days, 2 -> 7 days, 3 -> 14 days, 4+ -> 30 days
    // =========================================================================

    @Test
    @DisplayName("Revision count 0 should return 1 day interval")
    void shouldReturnOneDayWhenRevisionCountIsZero() {
        int interval = problemService.calculateRevisionInterval(0);
        assertEquals(1, interval);
    }

    @Test
    @DisplayName("Revision count 1 should return 3 days interval")
    void shouldReturnThreeDaysWhenRevisionCountIsOne() {
        int interval = problemService.calculateRevisionInterval(1);
        assertEquals(3, interval);
    }

    @Test
    @DisplayName("Revision count 2 should return 7 days interval")
    void shouldReturnSevenDaysWhenRevisionCountIsTwo() {
        int interval = problemService.calculateRevisionInterval(2);
        assertEquals(7, interval);
    }

    @Test
    @DisplayName("Revision count 3 should return 14 days interval")
    void shouldReturnFourteenDaysWhenRevisionCountIsThree() {
        int interval = problemService.calculateRevisionInterval(3);
        assertEquals(14, interval);
    }

    @Test
    @DisplayName("Revision count 4 or higher should return 30 days interval")
    void shouldReturnThirtyDaysWhenRevisionCountIsFourOrMore() {
        int intervalFour = problemService.calculateRevisionInterval(4);
        int intervalTen = problemService.calculateRevisionInterval(10);

        assertEquals(30, intervalFour);
        assertEquals(30, intervalTen);
    }

    // =========================================================================
    // 2. ProblemService Lifecycle & Ownership Tests
    // =========================================================================

    @Test
    @DisplayName("createProblem saves problem with logged-in user as owner")
    void shouldSaveProblemWithLoggedInUserAsOwner() {
        // Arrange: prepare a request to create a new problem
        ProblemRequest request = new ProblemRequest(
                "Two Sum", "Arrays", "Easy", "https://leetcode.com/problems/two-sum", "Pending", "Use hashmap"
        );

        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> {
            Problem p = invocation.getArgument(0);
            p.setId(101L);
            return p;
        });

        // Act: call the service method
        ProblemResponse response = problemService.createProblem(request);

        // Assert: verify response DTO and verify the saved entity belongs to testUser
        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Two Sum", response.getTitle());

        ArgumentCaptor<Problem> problemCaptor = ArgumentCaptor.forClass(Problem.class);
        verify(problemRepository).save(problemCaptor.capture());
        Problem savedEntity = problemCaptor.getValue();

        assertNotNull(savedEntity.getUser());
        assertEquals(testUser.getId(), savedEntity.getUser().getId());
        assertEquals(testUser.getEmail(), savedEntity.getUser().getEmail());
    }

    @Test
    @DisplayName("getProblemById throws ResourceNotFoundException when problem belongs to another user")
    void shouldThrowResourceNotFoundExceptionWhenProblemBelongsToAnotherUser() {
        // Arrange: problem 99 belongs to another user or doesn't exist for testUser
        when(problemRepository.findByIdAndUserId(99L, testUser.getId())).thenReturn(Optional.empty());

        // Act & Assert: requesting it should throw ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> problemService.getProblemById(99L));
    }

    @Test
    @DisplayName("markAsSolved sets status to Solved, solvedDate to today, and revisionCount to 0")
    void shouldSetStatusSolvedDateAndRevisionCountZeroWhenMarkAsSolved() {
        // Arrange: problem exists with status Pending and revisionCount 5
        Problem existingProblem = new Problem("Reverse Linked List", "LinkedList", "Easy", "link", "Pending", "notes");
        existingProblem.setId(10L);
        existingProblem.setRevisionCount(5);
        existingProblem.setSolvedDate(null);
        existingProblem.setUser(testUser);

        when(problemRepository.findByIdAndUserId(10L, testUser.getId())).thenReturn(Optional.of(existingProblem));
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: mark the problem as solved
        ProblemResponse response = problemService.markAsSolved(10L);

        // Assert: status must be Solved, solvedDate must be today, revisionCount must be reset to 0
        assertEquals("Solved", response.getStatus());
        assertEquals(LocalDate.now(), response.getSolvedDate());
        assertEquals(0, response.getRevisionCount());
    }

    @Test
    @DisplayName("markAsRevised increases revisionCount by 1 and updates lastRevisedDate")
    void shouldIncreaseRevisionCountByOneWhenMarkAsRevised() {
        // Arrange: problem already solved with revisionCount 2
        Problem existingProblem = new Problem("Binary Search", "Searching", "Easy", "link", "Solved", "notes");
        existingProblem.setId(20L);
        existingProblem.setRevisionCount(2);
        existingProblem.setUser(testUser);

        when(problemRepository.findByIdAndUserId(20L, testUser.getId())).thenReturn(Optional.of(existingProblem));
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: mark as revised
        ProblemResponse response = problemService.markAsRevised(20L);

        // Assert: revisionCount must increment from 2 to 3, lastRevisedDate must be today
        assertEquals(3, response.getRevisionCount());
        assertEquals(LocalDate.now(), response.getLastRevisedDate());
    }

    // =========================================================================
    // 3. revise-today Overdue Logic Tests
    // =========================================================================

    @Test
    @DisplayName("getProblemsDueForRevision includes due problems with daysOverdue and excludes future problems")
    void shouldIncludeDueProblemWithDaysOverdueAndExcludeFutureProblem() {
        // Arrange
        LocalDate today = LocalDate.now();

        // Problem 1: Due for revision (solved 5 days ago, revisionCount 0 -> interval 1 day -> due 4 days ago)
        Problem dueProblem = new Problem("LRU Cache", "Design", "Medium", "link", "Solved", "notes");
        dueProblem.setId(1L);
        dueProblem.setSolvedDate(today.minusDays(5));
        dueProblem.setLastRevisedDate(null);
        dueProblem.setRevisionCount(0);
        dueProblem.setUser(testUser);

        // Problem 2: NOT due yet (solved today, revisionCount 1 -> interval 3 days -> due in 3 days)
        Problem futureProblem = new Problem("Merge Intervals", "Arrays", "Medium", "link", "Solved", "notes");
        futureProblem.setId(2L);
        futureProblem.setSolvedDate(today);
        futureProblem.setLastRevisedDate(null);
        futureProblem.setRevisionCount(1);
        futureProblem.setUser(testUser);

        when(problemRepository.findByUserId(testUser.getId())).thenReturn(List.of(dueProblem, futureProblem));

        // Act: fetch problems due for revision
        List<ProblemRevisionResponse> result = problemService.getProblemsDueForRevision();

        // Assert: only the due problem is returned with exactly 4 days overdue
        assertEquals(1, result.size());
        ProblemRevisionResponse returnedProblem = result.get(0);
        assertEquals(1L, returnedProblem.getId());
        assertEquals(4L, returnedProblem.getDaysOverdue());
        assertEquals(today.minusDays(4), returnedProblem.getNextRevisionDate());
    }

    // =========================================================================
    // 4. Stats: Weakest Topic Logic Tests
    // =========================================================================

    @Test
    @DisplayName("weakestTopic ignores topics with fewer than 2 problems")
    void shouldIgnoreTopicsWithFewerThanTwoProblemsWhenCalculatingWeakestTopic() {
        // Arrange
        when(problemRepository.countByUserId(testUser.getId())).thenReturn(8L);
        when(problemRepository.countSolvedProblemsByUser(testUser.getId())).thenReturn(4L);

        // Topic stats: [topic, totalProblems, solvedProblems]
        // "Graphs": 1 problem, 0 solved (0.0%) -> has < 2 problems, so it MUST BE IGNORED
        // "DP": 4 problems, 1 solved (25.0%)  -> has >= 2 problems, lowest eligible percentage
        // "Arrays": 3 problems, 3 solved (100.0%) -> has >= 2 problems
        List<Object[]> rawTopicStats = List.of(
                new Object[]{"Graphs", 1L, 0L},
                new Object[]{"DP", 4L, 1L},
                new Object[]{"Arrays", 3L, 3L}
        );

        when(problemRepository.countProblemsByTopicForUser(testUser.getId())).thenReturn(rawTopicStats);
        when(problemRepository.countProblemsByDifficultyForUser(testUser.getId())).thenReturn(Collections.emptyList());

        // Act: compute stats
        StatsResponse stats = problemService.getStats();

        // Assert: topic breakdown has all 3 topics, but weakestTopic ignores "Graphs" and selects "DP"
        assertEquals(3, stats.getTopicBreakdown().size());
        assertEquals("DP", stats.getWeakestTopic());
    }
}

