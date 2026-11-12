package com.disha.tracker.service;

import com.disha.tracker.dto.ProblemResponse;
import com.disha.tracker.dto.RevisionStatsResponse;
import com.disha.tracker.dto.TopicStrengthDto;
import com.disha.tracker.dto.TopicSummaryDto;
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
public class TopicService {

    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public TopicService(ProblemRepository problemRepository, UserRepository userRepository) {
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
     * Comparator for selecting the suggested problem among candidates where canRevise is true and not mastered:
     * 1) Never revised, oldest solvedDate first.
     * 2) Otherwise lowest revisionCount, then oldest lastRevisedDate.
     */
    public static final Comparator<Problem> SUGGESTED_COMPARATOR = (p1, p2) -> {
        boolean p1Never = (p1.getRevisionCount() == 0);
        boolean p2Never = (p2.getRevisionCount() == 0);

        if (p1Never && !p2Never) return -1;
        if (!p1Never && p2Never) return 1;

        if (p1Never && p2Never) {
            LocalDate d1 = p1.getSolvedDate();
            LocalDate d2 = p2.getSolvedDate();
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return d1.compareTo(d2);
        }

        int countCmp = Integer.compare(p1.getRevisionCount(), p2.getRevisionCount());
        if (countCmp != 0) {
            return countCmp;
        }

        LocalDate r1 = p1.getLastRevisedDate();
        LocalDate r2 = p2.getLastRevisedDate();
        if (r1 == null && r2 == null) return 0;
        if (r1 == null) return 1;
        if (r2 == null) return -1;
        return r1.compareTo(r2);
    };

    /**
     * GET /api/topics/summary
     * For each topic the user has solved problems in:
     * topic, totalSolved, revisedCount, readyToReviseCount, masteredCount,
     * lastSolvedDate, lastRevisedDate, daysSinceLastActivity, strengthScore,
     * strengthLabel, and suggestedProblem.
     * Sorted by highest daysSinceLastActivity first.
     */
    public List<TopicSummaryDto> getTopicsSummary() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();

        List<Problem> userProblems = problemRepository.findByUserId(user.getId());

        // Group only solved problems with non-blank topic
        Map<String, List<Problem>> solvedByTopic = userProblems.stream()
                .filter(p -> p.getStatus() != null && "Solved".equalsIgnoreCase(p.getStatus()))
                .filter(p -> p.getTopic() != null && !p.getTopic().isBlank())
                .collect(Collectors.groupingBy(p -> p.getTopic().trim()));

        List<TopicSummaryDto> summaries = new ArrayList<>();

        for (Map.Entry<String, List<Problem>> entry : solvedByTopic.entrySet()) {
            String topic = entry.getKey();
            List<Problem> problems = entry.getValue();

            long totalSolved = problems.size();
            long revisedCount = problems.stream().filter(p -> p.getRevisionCount() >= 1).count();
            long readyToReviseCount = problems.stream().filter(p -> RevisionRules.canRevise(p, today)).count();
            long masteredCount = problems.stream().filter(RevisionRules::isMastered).count();

            LocalDate lastSolvedDate = problems.stream()
                    .map(Problem::getSolvedDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(null);

            LocalDate lastRevisedDate = problems.stream()
                    .map(Problem::getLastRevisedDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(null);

            LocalDate lastActivityDate = lastSolvedDate;
            if (lastRevisedDate != null) {
                if (lastActivityDate == null || lastRevisedDate.isAfter(lastActivityDate)) {
                    lastActivityDate = lastRevisedDate;
                }
            }

            long daysSinceLastActivity = (lastActivityDate != null)
                    ? Math.max(0L, ChronoUnit.DAYS.between(lastActivityDate, today))
                    : 0L;

            // Average revisions: Mastered problems count as revisionCount 3
            int sumEffectiveRevisions = problems.stream()
                    .mapToInt(p -> Math.min(p.getRevisionCount(), RevisionRules.MASTERED_REVISION_COUNT))
                    .sum();
            double averageRevisionCount = (totalSolved > 0)
                    ? (double) sumEffectiveRevisions / totalSolved
                    : 0.0;

            double strengthScore = RevisionRules.calculateStrengthScore(
                    totalSolved, revisedCount, averageRevisionCount, daysSinceLastActivity);
            String strengthLabel = RevisionRules.getStrengthLabel(strengthScore);

            // Suggested problem: canRevise true and mastered false
            Problem suggested = problems.stream()
                    .filter(p -> RevisionRules.canRevise(p, today) && !RevisionRules.isMastered(p))
                    .min(SUGGESTED_COMPARATOR)
                    .orElse(null);

            ProblemResponse suggestedDto = null;
            if (suggested != null) {
                suggestedDto = ProblemResponse.fromEntity(suggested);
                suggestedDto.setSuggested(true);
            }

            summaries.add(new TopicSummaryDto(
                    topic,
                    totalSolved,
                    revisedCount,
                    readyToReviseCount,
                    masteredCount,
                    lastSolvedDate,
                    lastRevisedDate,
                    daysSinceLastActivity,
                    strengthScore,
                    strengthLabel,
                    suggestedDto
            ));
        }

        // Sort by highest daysSinceLastActivity first
        summaries.sort(Comparator
                .comparingLong(TopicSummaryDto::getDaysSinceLastActivity)
                .reversed()
                .thenComparing(Comparator.comparingLong(TopicSummaryDto::getTotalSolved).reversed()));

        return summaries;
    }

    /**
     * GET /api/topics/problems?topic=<name>
     * Returns topic's problems ordered:
     * 1) Suggested problem first (highlighted)
     * 2) Revised problems by lastRevisedDate DESC (most recent first)
     * 3) Never-revised problems by solvedDate ASC (oldest first)
     * 4) Mastered problems last with Mastered badge
     */
    public List<ProblemResponse> getTopicProblems(String topicName) {
        if (topicName == null || topicName.isBlank()) {
            throw new IllegalArgumentException("Topic name parameter is required");
        }

        User user = getCurrentUser();
        LocalDate today = LocalDate.now();

        List<Problem> userProblems = problemRepository.findByUserId(user.getId());

        // Filter for this topic (case-insensitive)
        List<Problem> topicProblems = userProblems.stream()
                .filter(p -> p.getTopic() != null && p.getTopic().trim().equalsIgnoreCase(topicName.trim()))
                .toList();

        // 1. Identify the suggested problem (only solved, canRevise true, mastered false)
        Problem suggestedProblem = topicProblems.stream()
                .filter(p -> p.getStatus() != null && "Solved".equalsIgnoreCase(p.getStatus()))
                .filter(p -> RevisionRules.canRevise(p, today) && !RevisionRules.isMastered(p))
                .min(SUGGESTED_COMPARATOR)
                .orElse(null);

        Long suggestedId = (suggestedProblem != null) ? suggestedProblem.getId() : null;

        // 2. Revised problems (revisionCount > 0 && not mastered && not suggested), sorted by lastRevisedDate DESC
        List<Problem> revisedProblems = topicProblems.stream()
                .filter(p -> !Objects.equals(p.getId(), suggestedId))
                .filter(p -> !RevisionRules.isMastered(p) && p.getRevisionCount() > 0)
                .sorted((a, b) -> {
                    LocalDate da = a.getLastRevisedDate();
                    LocalDate db = b.getLastRevisedDate();
                    if (da == null && db == null) return 0;
                    if (da == null) return 1;
                    if (db == null) return -1;
                    return db.compareTo(da); // DESC
                })
                .toList();

        // 3. Never-revised problems (revisionCount == 0 && not mastered && not suggested), sorted by solvedDate ASC (oldest first)
        List<Problem> neverRevisedProblems = topicProblems.stream()
                .filter(p -> !Objects.equals(p.getId(), suggestedId))
                .filter(p -> !RevisionRules.isMastered(p) && p.getRevisionCount() == 0)
                .sorted((a, b) -> {
                    LocalDate da = a.getSolvedDate();
                    LocalDate db = b.getSolvedDate();
                    if (da == null && db == null) return 0;
                    if (da == null) return 1;
                    if (db == null) return -1;
                    return da.compareTo(db); // ASC (oldest first)
                })
                .toList();

        // 4. Mastered problems (mastered && not suggested), placed last
        List<Problem> masteredProblems = topicProblems.stream()
                .filter(p -> !Objects.equals(p.getId(), suggestedId))
                .filter(RevisionRules::isMastered)
                .sorted((a, b) -> {
                    LocalDate da = a.getLastRevisedDate();
                    LocalDate db = b.getLastRevisedDate();
                    if (da == null && db == null) return 0;
                    if (da == null) return 1;
                    if (db == null) return -1;
                    return db.compareTo(da); // DESC
                })
                .toList();

        List<ProblemResponse> result = new ArrayList<>();

        if (suggestedProblem != null) {
            ProblemResponse suggestedDto = ProblemResponse.fromEntity(suggestedProblem);
            suggestedDto.setSuggested(true);
            result.add(suggestedDto);
        }

        for (Problem p : revisedProblems) {
            result.add(ProblemResponse.fromEntity(p));
        }

        for (Problem p : neverRevisedProblems) {
            result.add(ProblemResponse.fromEntity(p));
        }

        for (Problem p : masteredProblems) {
            result.add(ProblemResponse.fromEntity(p));
        }

        return result;
    }

    /**
     * GET /api/revision/stats
     * Returns totalRevisionsDone, problemsRevisedAtLeastOnce, problemsNeverRevised,
     * problemsMastered, averageRevisionsPerProblem, revisionsLast7Days,
     * topicStrengths, strongestTopic, weakestTopic (ignoring topics with < 2 problems).
     */
    public RevisionStatsResponse getRevisionStats() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);

        List<Problem> userProblems = problemRepository.findByUserId(user.getId());

        List<Problem> solvedProblems = userProblems.stream()
                .filter(p -> p.getStatus() != null && "Solved".equalsIgnoreCase(p.getStatus()))
                .toList();

        long totalSolved = solvedProblems.size();
        long totalRevisionsDone = solvedProblems.stream().mapToLong(Problem::getRevisionCount).sum();
        long problemsRevisedAtLeastOnce = solvedProblems.stream().filter(p -> p.getRevisionCount() >= 1).count();
        long problemsNeverRevised = solvedProblems.stream().filter(p -> p.getRevisionCount() == 0).count();
        long problemsMastered = solvedProblems.stream().filter(RevisionRules::isMastered).count();

        double averageRevisionsPerProblem = (totalSolved > 0)
                ? Math.round(((double) totalRevisionsDone / totalSolved) * 100.0) / 100.0
                : 0.0;

        long revisionsLast7Days = solvedProblems.stream()
                .map(Problem::getLastRevisedDate)
                .filter(Objects::nonNull)
                .filter(d -> !d.isBefore(sevenDaysAgo))
                .count();

        // Calculate strength per topic
        Map<String, List<Problem>> solvedByTopic = solvedProblems.stream()
                .filter(p -> p.getTopic() != null && !p.getTopic().isBlank())
                .collect(Collectors.groupingBy(p -> p.getTopic().trim()));

        List<TopicStrengthDto> topicStrengths = new ArrayList<>();
        String strongestTopic = null;
        double maxStrength = -1.0;
        long maxStrengthTotal = -1;

        String weakestTopic = null;
        double minStrength = Double.MAX_VALUE;
        long minStrengthTotal = Long.MAX_VALUE;

        for (Map.Entry<String, List<Problem>> entry : solvedByTopic.entrySet()) {
            String topic = entry.getKey();
            List<Problem> problems = entry.getValue();

            long countInTopic = problems.size();
            long revisedCount = problems.stream().filter(p -> p.getRevisionCount() >= 1).count();

            LocalDate lastSolved = problems.stream().map(Problem::getSolvedDate).filter(Objects::nonNull).max(LocalDate::compareTo).orElse(null);
            LocalDate lastRevised = problems.stream().map(Problem::getLastRevisedDate).filter(Objects::nonNull).max(LocalDate::compareTo).orElse(null);

            LocalDate lastAct = lastSolved;
            if (lastRevised != null && (lastAct == null || lastRevised.isAfter(lastAct))) {
                lastAct = lastRevised;
            }
            long activityDays = (lastAct != null) ? Math.max(0L, ChronoUnit.DAYS.between(lastAct, today)) : 0L;

            int sumEffective = problems.stream().mapToInt(p -> Math.min(p.getRevisionCount(), RevisionRules.MASTERED_REVISION_COUNT)).sum();
            double avgRev = (countInTopic > 0) ? (double) sumEffective / countInTopic : 0.0;

            double score = RevisionRules.calculateStrengthScore(countInTopic, revisedCount, avgRev, activityDays);
            String label = RevisionRules.getStrengthLabel(score);

            topicStrengths.add(new TopicStrengthDto(topic, score, label, countInTopic));

            // Strongest / weakest topic ignores topics with fewer than 2 problems
            if (countInTopic >= 2) {
                if (score > maxStrength || (score == maxStrength && countInTopic > maxStrengthTotal)) {
                    maxStrength = score;
                    maxStrengthTotal = countInTopic;
                    strongestTopic = topic;
                }

                if (score < minStrength || (score == minStrength && countInTopic < minStrengthTotal)) {
                    minStrength = score;
                    minStrengthTotal = countInTopic;
                    weakestTopic = topic;
                }
            }
        }

        // Sort topic strengths descending by score
        topicStrengths.sort(Comparator.comparingDouble(TopicStrengthDto::getStrengthScore).reversed());

        return new RevisionStatsResponse(
                totalRevisionsDone,
                problemsRevisedAtLeastOnce,
                problemsNeverRevised,
                problemsMastered,
                averageRevisionsPerProblem,
                revisionsLast7Days,
                topicStrengths,
                strongestTopic,
                weakestTopic
        );
    }
}

