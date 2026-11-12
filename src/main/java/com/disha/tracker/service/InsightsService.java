package com.disha.tracker.service;

import com.disha.tracker.dto.TopicInsightDto;
import com.disha.tracker.dto.TopicInsightsResponse;
import com.disha.tracker.entity.Problem;
import com.disha.tracker.entity.User;
import com.disha.tracker.exception.ResourceNotFoundException;
import com.disha.tracker.repository.ProblemRepository;
import com.disha.tracker.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InsightsService {

    // Threshold constants for topic revision status
    public static final int FRESH_THRESHOLD_DAYS = 7;
    public static final int DUE_SOON_THRESHOLD_DAYS = 14;
    public static final int MIN_SOLVED_FOR_NEGLECTED = 3;

    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public InsightsService(ProblemRepository problemRepository, UserRepository userRepository) {
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    // Helper: extracts currently authenticated user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Compute comprehensive topic insights for the logged-in user.
     * Evaluates solve counts, 30-day activity, days since last solve/revision,
     * freshness status, most neglected topic, and strongest topic.
     */
    public TopicInsightsResponse getTopicInsights() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        List<Problem> userProblems = problemRepository.findByUserId(user.getId());

        // Group only solved problems with a non-blank topic
        Map<String, List<Problem>> solvedByTopic = userProblems.stream()
                .filter(p -> p.getStatus() != null && "Solved".equalsIgnoreCase(p.getStatus()))
                .filter(p -> p.getTopic() != null && !p.getTopic().isBlank())
                .collect(Collectors.groupingBy(p -> p.getTopic().trim()));

        List<TopicInsightDto> topicDtos = new ArrayList<>();

        for (Map.Entry<String, List<Problem>> entry : solvedByTopic.entrySet()) {
            String topic = entry.getKey();
            List<Problem> problems = entry.getValue();

            long totalSolved = problems.size();

            // Count problems solved in last 30 days
            long solvedLast30Days = problems.stream()
                    .map(Problem::getSolvedDate)
                    .filter(Objects::nonNull)
                    .filter(d -> !d.isBefore(thirtyDaysAgo))
                    .count();

            // Latest solved date
            LocalDate lastSolvedDate = problems.stream()
                    .map(Problem::getSolvedDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(null);

            Long daysSinceLastSolved = (lastSolvedDate != null)
                    ? ChronoUnit.DAYS.between(lastSolvedDate, today)
                    : null;

            // Latest revised date (may be null if never revised)
            LocalDate lastRevisedDate = problems.stream()
                    .map(Problem::getLastRevisedDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(null);

            // Latest activity is the later of lastSolvedDate and lastRevisedDate
            LocalDate lastActivityDate = lastSolvedDate;
            if (lastRevisedDate != null) {
                if (lastActivityDate == null || lastRevisedDate.isAfter(lastActivityDate)) {
                    lastActivityDate = lastRevisedDate;
                }
            }

            long daysSinceLastActivity = (lastActivityDate != null)
                    ? ChronoUnit.DAYS.between(lastActivityDate, today)
                    : 0L;

            // Determine revision status
            String revisionStatus;
            if (daysSinceLastActivity <= FRESH_THRESHOLD_DAYS) {
                revisionStatus = "FRESH";
            } else if (daysSinceLastActivity <= DUE_SOON_THRESHOLD_DAYS) {
                revisionStatus = "DUE_SOON";
            } else {
                revisionStatus = "OVERDUE";
            }

            topicDtos.add(new TopicInsightDto(
                    topic,
                    totalSolved,
                    solvedLast30Days,
                    lastSolvedDate,
                    daysSinceLastSolved,
                    lastRevisedDate,
                    daysSinceLastActivity,
                    revisionStatus
            ));
        }

        // Sort by most overdue first (highest daysSinceLastActivity first, tie-break by totalSolved DESC)
        topicDtos.sort(Comparator
                .comparingLong(TopicInsightDto::getDaysSinceLastActivity)
                .reversed()
                .thenComparing(Comparator.comparingLong(TopicInsightDto::getTotalSolved).reversed()));

        // Find mostNeglectedTopic (highest daysSinceLastActivity among topics with at least MIN_SOLVED_FOR_NEGLECTED (3) solved)
        String mostNeglectedTopic = null;
        Long mostNeglectedDays = null;
        long maxNeglectedDays = -1;

        for (TopicInsightDto dto : topicDtos) {
            if (dto.getTotalSolved() >= MIN_SOLVED_FOR_NEGLECTED) {
                if (dto.getDaysSinceLastActivity() > maxNeglectedDays) {
                    maxNeglectedDays = dto.getDaysSinceLastActivity();
                    mostNeglectedTopic = dto.getTopic();
                    mostNeglectedDays = maxNeglectedDays;
                }
            }
        }

        // Find strongestTopic (highest totalSolved)
        String strongestTopic = null;
        Long strongestTopicSolved = null;
        long maxSolved = -1;

        for (TopicInsightDto dto : topicDtos) {
            if (dto.getTotalSolved() > maxSolved) {
                maxSolved = dto.getTotalSolved();
                strongestTopic = dto.getTopic();
                strongestTopicSolved = maxSolved;
            }
        }

        return new TopicInsightsResponse(
                topicDtos,
                mostNeglectedTopic,
                mostNeglectedDays,
                strongestTopic,
                strongestTopicSolved
        );
    }
}

