package com.disha.tracker.service;

import com.disha.tracker.dto.TopicInsightDto;
import com.disha.tracker.dto.TopicInsightsResponse;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightsServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InsightsService insightsService;

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
    @DisplayName("Day calculation resets daysSinceLastActivity when lastRevisedDate is more recent than lastSolvedDate")
    void shouldCalculateDaysSinceLastActivityUsingRevisionDateWhenMoreRecent() {
        LocalDate today = LocalDate.now();

        // Solved 30 days ago, but revised 3 days ago
        Problem problem = new Problem("Reverse Linked List", "Linked List", "Easy", "link", "Solved", "notes");
        problem.setSolvedDate(today.minusDays(30));
        problem.setLastRevisedDate(today.minusDays(3));
        problem.setUser(testUser);

        when(problemRepository.findByUserId(testUser.getId())).thenReturn(List.of(problem));

        TopicInsightsResponse response = insightsService.getTopicInsights();

        assertNotNull(response);
        assertEquals(1, response.getTopics().size());

        TopicInsightDto dto = response.getTopics().get(0);
        assertEquals("Linked List", dto.getTopic());
        assertEquals(30L, dto.getDaysSinceLastSolved());
        // Since revision was 3 days ago, activity days should be 3, NOT 30!
        assertEquals(3L, dto.getDaysSinceLastActivity());
        assertEquals("FRESH", dto.getRevisionStatus()); // 3 <= 7
    }

    @Test
    @DisplayName("Status thresholds correctly categorize FRESH (<=7), DUE_SOON (8-14), and OVERDUE (>14)")
    void shouldCategorizeRevisionStatusAccordingToThresholds() {
        LocalDate today = LocalDate.now();

        // 1. Fresh: 5 days ago (<= 7)
        Problem freshProb = new Problem("Two Sum", "Arrays", "Easy", "link", "Solved", "notes");
        freshProb.setSolvedDate(today.minusDays(5));
        freshProb.setUser(testUser);

        // 2. Due soon: 10 days ago (8-14)
        Problem dueSoonProb = new Problem("Invert Tree", "Trees", "Easy", "link", "Solved", "notes");
        dueSoonProb.setSolvedDate(today.minusDays(10));
        dueSoonProb.setUser(testUser);

        // 3. Overdue: 25 days ago (> 14)
        Problem overdueProb = new Problem("Coin Change", "Dynamic Programming", "Medium", "link", "Solved", "notes");
        overdueProb.setSolvedDate(today.minusDays(25));
        overdueProb.setUser(testUser);

        when(problemRepository.findByUserId(testUser.getId())).thenReturn(List.of(freshProb, dueSoonProb, overdueProb));

        TopicInsightsResponse response = insightsService.getTopicInsights();

        // Topics are sorted by most overdue first
        List<TopicInsightDto> topics = response.getTopics();
        assertEquals(3, topics.size());

        assertEquals("Dynamic Programming", topics.get(0).getTopic());
        assertEquals(25L, topics.get(0).getDaysSinceLastActivity());
        assertEquals("OVERDUE", topics.get(0).getRevisionStatus());

        assertEquals("Trees", topics.get(1).getTopic());
        assertEquals(10L, topics.get(1).getDaysSinceLastActivity());
        assertEquals("DUE_SOON", topics.get(1).getRevisionStatus());

        assertEquals("Arrays", topics.get(2).getTopic());
        assertEquals(5L, topics.get(2).getDaysSinceLastActivity());
        assertEquals("FRESH", topics.get(2).getRevisionStatus());
    }

    @Test
    @DisplayName("mostNeglectedTopic ignores topics with fewer than 3 solved problems")
    void shouldSelectMostNeglectedTopicOnlyAmongTopicsWithAtLeastThreeSolved() {
        LocalDate today = LocalDate.now();

        // Graphs: 1 solved problem, 100 days old -> Must be IGNORED because < 3 solved
        Problem graphProb = new Problem("Dijkstra", "Graphs", "Medium", "link", "Solved", "notes");
        graphProb.setSolvedDate(today.minusDays(100));
        graphProb.setUser(testUser);

        // Trees: 3 solved problems, 35 days old -> ELIGIBLE and should be selected!
        Problem tree1 = new Problem("Tree 1", "Trees", "Easy", "link", "Solved", "notes");
        tree1.setSolvedDate(today.minusDays(50));
        tree1.setUser(testUser);

        Problem tree2 = new Problem("Tree 2", "Trees", "Medium", "link", "Solved", "notes");
        tree2.setSolvedDate(today.minusDays(40));
        tree2.setUser(testUser);

        Problem tree3 = new Problem("Tree 3", "Trees", "Hard", "link", "Solved", "notes");
        tree3.setSolvedDate(today.minusDays(35)); // latest activity for Trees = 35 days ago
        tree3.setUser(testUser);

        // Arrays: 5 solved problems, 5 days old
        Problem arr1 = new Problem("Arr 1", "Arrays", "Easy", "link", "Solved", "notes");
        arr1.setSolvedDate(today.minusDays(5));
        arr1.setUser(testUser);
        Problem arr2 = new Problem("Arr 2", "Arrays", "Easy", "link", "Solved", "notes");
        arr2.setSolvedDate(today.minusDays(5));
        arr2.setUser(testUser);
        Problem arr3 = new Problem("Arr 3", "Arrays", "Easy", "link", "Solved", "notes");
        arr3.setSolvedDate(today.minusDays(5));
        arr3.setUser(testUser);
        Problem arr4 = new Problem("Arr 4", "Arrays", "Easy", "link", "Solved", "notes");
        arr4.setSolvedDate(today.minusDays(5));
        arr4.setUser(testUser);
        Problem arr5 = new Problem("Arr 5", "Arrays", "Easy", "link", "Solved", "notes");
        arr5.setSolvedDate(today.minusDays(5));
        arr5.setUser(testUser);

        when(problemRepository.findByUserId(testUser.getId())).thenReturn(List.of(
                graphProb, tree1, tree2, tree3, arr1, arr2, arr3, arr4, arr5
        ));

        TopicInsightsResponse response = insightsService.getTopicInsights();

        // Verify most neglected topic is Trees (35 days), ignoring Graphs (100 days)
        assertEquals("Trees", response.getMostNeglectedTopic());
        assertEquals(35L, response.getMostNeglectedDays());

        // Verify strongest topic is Arrays (5 solved)
        assertEquals("Arrays", response.getStrongestTopic());
        assertEquals(5L, response.getStrongestTopicSolved());
    }

    @Test
    @DisplayName("Returns empty insights when user has no solved problems")
    void shouldReturnEmptyWhenNoSolvedProblems() {
        when(problemRepository.findByUserId(testUser.getId())).thenReturn(List.of());

        TopicInsightsResponse response = insightsService.getTopicInsights();

        assertNotNull(response);
        assertEquals(0, response.getTopics().size());
        assertNull(response.getMostNeglectedTopic());
        assertNull(response.getStrongestTopic());
    }
}

