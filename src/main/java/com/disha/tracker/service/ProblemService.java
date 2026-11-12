package com.disha.tracker.service;

import com.disha.tracker.dto.DifficultyStats;
import com.disha.tracker.dto.ProblemRequest;
import com.disha.tracker.dto.ProblemResponse;
import com.disha.tracker.dto.ProblemRevisionResponse;
import com.disha.tracker.dto.StatsResponse;
import com.disha.tracker.dto.TopicStats;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public ProblemService(ProblemRepository problemRepository, UserRepository userRepository) {
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    // Helper: extracts the currently authenticated user from SecurityContextHolder
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    // 1. Get all problems for the logged-in user
    public List<ProblemResponse> getAllProblems() {
        User user = getCurrentUser();
        return problemRepository.findByUserId(user.getId())
                .stream()
                .map(ProblemResponse::fromEntity)
                .toList();
    }

    // 2. Get problems for the logged-in user with optional filters (topic, difficulty, status)
    public List<ProblemResponse> getProblems(String topic, String difficulty, String status) {
        User user = getCurrentUser();
        String cleanTopic = (topic != null && !topic.isBlank()) ? topic.trim() : null;
        String cleanDifficulty = (difficulty != null && !difficulty.isBlank()) ? difficulty.trim() : null;
        String cleanStatus = (status != null && !status.isBlank()) ? status.trim() : null;

        return problemRepository.findByUserAndFilters(user.getId(), cleanTopic, cleanDifficulty, cleanStatus)
                .stream()
                .map(ProblemResponse::fromEntity)
                .toList();
    }

    // 3. Get a single problem by ID (only if owned by logged-in user, otherwise 404)
    public ProblemResponse getProblemById(Long id) {
        User user = getCurrentUser();
        Problem problem = problemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
        return ProblemResponse.fromEntity(problem);
    }

    // 4. Create a new problem linked to the logged-in user
    public ProblemResponse createProblem(ProblemRequest request) {
        User user = getCurrentUser();

        Problem problem = new Problem();
        problem.setTitle(request.getTitle());
        problem.setTopic(request.getTopic());
        problem.setDifficulty(request.getDifficulty());
        problem.setLink(request.getLink());
        problem.setStatus(request.getStatus());
        problem.setNotes(request.getNotes());
        problem.setSolvedDate(request.getSolvedDate());
        problem.setLastRevisedDate(request.getLastRevisedDate());
        problem.setRevisionCount(request.getRevisionCount());
        problem.setUser(user);

        Problem savedProblem = problemRepository.save(problem);
        return ProblemResponse.fromEntity(savedProblem);
    }

    // 5. Update an existing problem (only if owned by logged-in user, otherwise 404)
    public ProblemResponse updateProblem(Long id, ProblemRequest request) {
        User user = getCurrentUser();
        Problem existingProblem = problemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        existingProblem.setTitle(request.getTitle());
        existingProblem.setTopic(request.getTopic());
        existingProblem.setDifficulty(request.getDifficulty());
        existingProblem.setLink(request.getLink());
        existingProblem.setStatus(request.getStatus());
        existingProblem.setNotes(request.getNotes());
        existingProblem.setSolvedDate(request.getSolvedDate());
        existingProblem.setLastRevisedDate(request.getLastRevisedDate());
        existingProblem.setRevisionCount(request.getRevisionCount());

        Problem updatedProblem = problemRepository.save(existingProblem);
        return ProblemResponse.fromEntity(updatedProblem);
    }

    // 6. Delete a problem by ID (only if owned by logged-in user, otherwise 404)
    public void deleteProblem(Long id) {
        User user = getCurrentUser();
        Problem problem = problemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
        problemRepository.delete(problem);
    }

    // 7. Mark a problem as solved (only if owned by logged-in user, otherwise 404)
    public ProblemResponse markAsSolved(Long id) {
        User user = getCurrentUser();
        Problem problem = problemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        problem.setStatus("Solved");
        problem.setSolvedDate(LocalDate.now());
        problem.setRevisionCount(0);

        Problem savedProblem = problemRepository.save(problem);
        return ProblemResponse.fromEntity(savedProblem);
    }

    // 8. Mark a problem as revised (only if owned by logged-in user, otherwise 404)
    public ProblemResponse markAsRevised(Long id) {
        User user = getCurrentUser();
        Problem problem = problemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        if (RevisionRules.isMastered(problem)) {
            throw new IllegalArgumentException("Problem is already mastered");
        }

        LocalDate today = LocalDate.now();
        if (!RevisionRules.canRevise(problem, today)) {
            LocalDate availableDate = RevisionRules.getAvailableDate(problem);
            throw new IllegalArgumentException("Problem is locked for revision. Available again on " + availableDate);
        }

        problem.setLastRevisedDate(today);
        problem.setRevisionCount(problem.getRevisionCount() + 1);

        Problem savedProblem = problemRepository.save(problem);
        return ProblemResponse.fromEntity(savedProblem);
    }

    // 9. Get preparation stats, topic breakdown, difficulty breakdown, and weakest topic for logged-in user
    public StatsResponse getStats() {
        User user = getCurrentUser();
        Long userId = user.getId();

        // Overall problem counts for this user
        long totalProblems = problemRepository.countByUserId(userId);
        long solvedCount = problemRepository.countSolvedProblemsByUser(userId);
        long pendingCount = totalProblems - solvedCount;

        // Topic breakdown for this user
        List<Object[]> rawTopicStats = problemRepository.countProblemsByTopicForUser(userId);
        List<TopicStats> topicBreakdown = new ArrayList<>();

        String weakestTopic = null;
        double minSolvedPercentage = Double.MAX_VALUE;

        for (Object[] row : rawTopicStats) {
            String topic = (String) row[0];
            long total = ((Number) row[1]).longValue();
            long solved = (row[2] != null) ? ((Number) row[2]).longValue() : 0L;

            // Calculate percentage rounded to 2 decimal places: e.g. 66.67
            double percentage = (total == 0) ? 0.0 : Math.round(((double) solved / total) * 10000.0) / 100.0;

            topicBreakdown.add(new TopicStats(topic, total, solved, percentage));

            // Weakest topic: lowest solvedPercentage among topics with at least 2 problems
            if (total >= 2) {
                if (percentage < minSolvedPercentage) {
                    minSolvedPercentage = percentage;
                    weakestTopic = topic;
                }
            }
        }

        // Difficulty breakdown (Easy, Medium, Hard)
        Map<String, DifficultyStats> difficultyBreakdown = new LinkedHashMap<>();
        difficultyBreakdown.put("Easy", new DifficultyStats(0, 0));
        difficultyBreakdown.put("Medium", new DifficultyStats(0, 0));
        difficultyBreakdown.put("Hard", new DifficultyStats(0, 0));

        List<Object[]> rawDifficultyStats = problemRepository.countProblemsByDifficultyForUser(userId);
        for (Object[] row : rawDifficultyStats) {
            String difficulty = (String) row[0];
            long total = ((Number) row[1]).longValue();
            long solved = (row[2] != null) ? ((Number) row[2]).longValue() : 0L;

            if (difficulty != null) {
                for (String key : difficultyBreakdown.keySet()) {
                    if (key.equalsIgnoreCase(difficulty)) {
                        difficultyBreakdown.put(key, new DifficultyStats(total, solved));
                        break;
                    }
                }
            }
        }

        return new StatsResponse(
                totalProblems,
                solvedCount,
                pendingCount,
                topicBreakdown,
                difficultyBreakdown,
                weakestTopic
        );
    }
}
