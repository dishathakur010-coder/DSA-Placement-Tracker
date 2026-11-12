package com.disha.tracker.service;

import com.disha.tracker.dto.CodeforcesProfileResponse;
import com.disha.tracker.dto.SyncSummaryResponse;
import com.disha.tracker.dto.codeforces.CodeforcesProblem;
import com.disha.tracker.dto.codeforces.CodeforcesResponse;
import com.disha.tracker.dto.codeforces.CodeforcesSubmission;
import com.disha.tracker.entity.Problem;
import com.disha.tracker.entity.User;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeforcesSyncServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CodeforcesSyncService codeforcesSyncService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Disha");
        testUser.setEmail("disha@example.com");
        testUser.setCodeforcesHandle("tourist");

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
    @DisplayName("saveHandle updates user's Codeforces handle")
    void shouldSaveHandleSuccessfully() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CodeforcesProfileResponse response = codeforcesSyncService.saveHandle("tourist_new");

        assertEquals("tourist_new", response.getHandle());
        assertEquals("tourist_new", testUser.getCodeforcesHandle());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("saveHandle throws IllegalArgumentException when handle is blank")
    void shouldThrowExceptionWhenHandleIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> codeforcesSyncService.saveHandle("   "));
    }

    @Test
    @DisplayName("syncCodeforces throws IllegalArgumentException when handle is not set")
    void shouldThrowExceptionWhenHandleNotSet() {
        testUser.setCodeforcesHandle(null);

        assertThrows(IllegalArgumentException.class, () -> codeforcesSyncService.syncCodeforces());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("syncCodeforces imports full history, filters non-OK verdicts, and dedupes")
    void shouldSyncFullHistoryAndDeduplicate() {
        long tenDaysAgoSeconds = Instant.now().minus(10, ChronoUnit.DAYS).getEpochSecond();
        long seventyDaysAgoSeconds = Instant.now().minus(70, ChronoUnit.DAYS).getEpochSecond();

        // 1. Problem A: solved 10 days ago with OK verdict -> should be imported
        CodeforcesProblem problemA = new CodeforcesProblem(1900L, "A", "Cover in Water", "PROGRAMMING", 800, List.of("math"));
        CodeforcesSubmission subA = new CodeforcesSubmission(101L, 1900L, tenDaysAgoSeconds, "OK", problemA);

        // 2. Problem B: solved 10 days ago but WRONG_ANSWER -> should be skipped
        CodeforcesProblem problemB = new CodeforcesProblem(1900L, "B", "Laura and Operations", "PROGRAMMING", 1100, List.of("dp"));
        CodeforcesSubmission subB = new CodeforcesSubmission(102L, 1900L, tenDaysAgoSeconds, "WRONG_ANSWER", problemB);

        // 3. Problem C: solved 70 days ago -> FULL HISTORY should be imported!
        CodeforcesProblem problemC = new CodeforcesProblem(1800L, "C", "Old Problem", "PROGRAMMING", 1300, List.of("trees"));
        CodeforcesSubmission subC = new CodeforcesSubmission(103L, 1800L, seventyDaysAgoSeconds, "OK", problemC);

        // 4. Problem D: already exists in DB for this user -> should be counted as skipped
        CodeforcesProblem problemD = new CodeforcesProblem(1700L, "D", "Existing Problem", "PROGRAMMING", 1500, List.of("binary search"));
        CodeforcesSubmission subD = new CodeforcesSubmission(104L, 1700L, tenDaysAgoSeconds, "OK", problemD);

        CodeforcesResponse fakeResponse = new CodeforcesResponse("OK", null, List.of(subA, subB, subC, subD));

        // Mock RestClient chain
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(Function.class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(CodeforcesResponse.class))).thenReturn(fakeResponse);

        // Problem 1700D already exists in DB for this user
        when(problemRepository.findExternalIdsByUserIdAndPlatform(testUser.getId(), "CODEFORCES"))
                .thenReturn(new HashSet<>(Set.of("1700D")));

        when(problemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SyncSummaryResponse summary = codeforcesSyncService.syncCodeforces();

        // Assert
        assertNotNull(summary);
        assertEquals(2, summary.getImportedCount()); // Problem A and Problem C
        assertEquals(1, summary.getSkippedCount());  // Problem D was in DB

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(problemRepository).saveAll(captor.capture());

        @SuppressWarnings("unchecked")
        List<Problem> savedProblems = captor.getValue();
        assertEquals(2, savedProblems.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("syncCodeforces retries once on timeout before succeeding")
    void shouldRetryOnceOnTimeoutBeforeSucceeding() {
        CodeforcesProblem problemA = new CodeforcesProblem(1900L, "A", "Problem A", "PROGRAMMING", 800, List.of("math"));
        CodeforcesSubmission subA = new CodeforcesSubmission(101L, 1900L, Instant.now().getEpochSecond(), "OK", problemA);
        CodeforcesResponse fakeResponse = new CodeforcesResponse("OK", null, List.of(subA));

        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(Function.class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);

        // 1st attempt throws ResourceAccessException (timeout), 2nd attempt succeeds
        when(responseSpec.body(eq(CodeforcesResponse.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"))
                .thenReturn(fakeResponse);

        when(problemRepository.findExternalIdsByUserIdAndPlatform(testUser.getId(), "CODEFORCES"))
                .thenReturn(new HashSet<>());
        when(problemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SyncSummaryResponse summary = codeforcesSyncService.syncCodeforces();

        assertNotNull(summary);
        assertEquals(1, summary.getImportedCount());
        verify(responseSpec, times(2)).body(eq(CodeforcesResponse.class));
    }
}
