package com.disha.tracker.service;

import com.disha.tracker.dto.ProblemRequest;
import com.disha.tracker.dto.ProblemResponse;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test Coder");
        testUser.setEmail("coder@example.com");

        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getName()).thenReturn("coder@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(userRepository.findByEmail("coder@example.com")).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // 1. ProblemService Lifecycle & Ownership Tests
    // =========================================================================

    @Test
    @DisplayName("createProblem saves problem with logged-in user as owner")
    void shouldSaveProblemWithLoggedInUserAsOwner() {
        ProblemRequest request = new ProblemRequest(
                "Two Sum", "Arrays", "Easy", "https://leetcode.com/problems/two-sum", "Pending", "Use hashmap"
        );

        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> {
            Problem p = invocation.getArgument(0);
            p.setId(101L);
            return p;
        });

        ProblemResponse response = problemService.createProblem(request);

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
        when(problemRepository.findByIdAndUserId(99L, testUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> problemService.getProblemById(99L));
    }

    @Test
    @DisplayName("markAsSolved sets status to Solved, solvedDate to today, and revisionCount to 0")
    void shouldSetStatusSolvedDateAndRevisionCountZeroWhenMarkAsSolved() {
        Problem existingProblem = new Problem("Reverse Linked List", "LinkedList", "Easy", "link", "Pending", "notes");
        existingProblem.setId(10L);
        existingProblem.setRevisionCount(5);
        existingProblem.setSolvedDate(null);
        existingProblem.setUser(testUser);

        when(problemRepository.findByIdAndUserId(10L, testUser.getId())).thenReturn(Optional.of(existingProblem));
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProblemResponse response = problemService.markAsSolved(10L);

        assertEquals("Solved", response.getStatus());
        assertEquals(LocalDate.now(), response.getSolvedDate());
        assertEquals(0, response.getRevisionCount());
    }

    // =========================================================================
    // 2. Revision Lock & Mastery Enforcement Tests
    // =========================================================================

    @Test
    @DisplayName("markAsRevised increases revisionCount by 1 and updates lastRevisedDate when unlocked")
    void shouldIncreaseRevisionCountByOneWhenUnlocked() {
        LocalDate today = LocalDate.now();

        // Problem with revisionCount 1, revised 7 days ago -> lock is 7 days, so it is unlocked!
        Problem existingProblem = new Problem("Binary Search", "Searching", "Easy", "link", "Solved", "notes");
        existingProblem.setId(20L);
        existingProblem.setRevisionCount(1);
        existingProblem.setSolvedDate(today.minusDays(20));
        existingProblem.setLastRevisedDate(today.minusDays(7));
        existingProblem.setUser(testUser);

        when(problemRepository.findByIdAndUserId(20L, testUser.getId())).thenReturn(Optional.of(existingProblem));
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProblemResponse response = problemService.markAsRevised(20L);

        assertEquals(2, response.getRevisionCount());
        assertEquals(today, response.getLastRevisedDate());
    }

    @Test
    @DisplayName("markAsRevised throws IllegalArgumentException with available date when problem is locked")
    void shouldThrowExceptionWhenProblemIsLocked() {
        LocalDate today = LocalDate.now();

        // Problem revised 3 days ago (revisionCount 1 -> locked for 7 days)
        Problem lockedProblem = new Problem("Merge Intervals", "Arrays", "Medium", "link", "Solved", "notes");
        lockedProblem.setId(25L);
        lockedProblem.setRevisionCount(1);
        lockedProblem.setSolvedDate(today.minusDays(10));
        lockedProblem.setLastRevisedDate(today.minusDays(3));
        lockedProblem.setUser(testUser);

        when(problemRepository.findByIdAndUserId(25L, testUser.getId())).thenReturn(Optional.of(lockedProblem));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> problemService.markAsRevised(25L));

        assertTrue(ex.getMessage().contains("Available again on"));
    }

    @Test
    @DisplayName("markAsRevised throws IllegalArgumentException when problem is already mastered")
    void shouldThrowExceptionWhenProblemIsMastered() {
        Problem masteredProblem = new Problem("LRU Cache", "Design", "Medium", "link", "Solved", "notes");
        masteredProblem.setId(30L);
        masteredProblem.setRevisionCount(3); // Mastered!
        masteredProblem.setUser(testUser);

        when(problemRepository.findByIdAndUserId(30L, testUser.getId())).thenReturn(Optional.of(masteredProblem));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> problemService.markAsRevised(30L));

        assertEquals("Problem is already mastered", ex.getMessage());
    }

    // =========================================================================
    // 3. Stats: Weakest Topic Logic Tests
    // =========================================================================

    @Test
    @DisplayName("weakestTopic ignores topics with fewer than 2 problems")
    void shouldIgnoreTopicsWithFewerThanTwoProblemsWhenCalculatingWeakestTopic() {
        when(problemRepository.countByUserId(testUser.getId())).thenReturn(8L);
        when(problemRepository.countSolvedProblemsByUser(testUser.getId())).thenReturn(4L);

        List<Object[]> rawTopicStats = List.of(
                new Object[]{"Graphs", 1L, 0L},
                new Object[]{"DP", 4L, 1L},
                new Object[]{"Arrays", 3L, 3L}
        );

        when(problemRepository.countProblemsByTopicForUser(testUser.getId())).thenReturn(rawTopicStats);
        when(problemRepository.countProblemsByDifficultyForUser(testUser.getId())).thenReturn(Collections.emptyList());

        StatsResponse stats = problemService.getStats();

        assertEquals(3, stats.getTopicBreakdown().size());
        assertEquals("DP", stats.getWeakestTopic());
    }
}
