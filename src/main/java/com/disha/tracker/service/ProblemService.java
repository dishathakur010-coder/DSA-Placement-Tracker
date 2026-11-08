package com.disha.tracker.service;

import com.disha.tracker.dto.DifficultyStats;
import com.disha.tracker.dto.ProblemRequest;
import com.disha.tracker.dto.ProblemResponse;
import com.disha.tracker.dto.ProblemRevisionResponse;
import com.disha.tracker.dto.StatsResponse;
import com.disha.tracker.dto.TopicStats;
import com.disha.tracker.entity.Problem;
import com.disha.tracker.exception.ResourceNotFoundException;
import com.disha.tracker.repository.ProblemRepository;
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

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    // 1. Get all problems as a list of ProblemResponse DTOs
    public List<ProblemResponse> getAllProblems() {
        return problemRepository.findAll()
                .stream()
                .map(ProblemResponse::fromEntity)
                .toList();
    }

    // 2. Get problems with optional filters (topic, difficulty, status)
    public List<ProblemResponse> getProblems(String topic, String difficulty, String status) {
        String cleanTopic = (topic != null && !topic.isBlank()) ? topic.trim() : null;
        String cleanDifficulty = (difficulty != null && !difficulty.isBlank()) ? difficulty.trim() : null;
        String cleanStatus = (status != null && !status.isBlank()) ? status.trim() : null;

        return problemRepository.findByFilters(cleanTopic, cleanDifficulty, cleanStatus)
                .stream()
                .map(ProblemResponse::fromEntity)
                .toList();
    }

    // 3. Get a single problem by ID, or throw 404 exception if not found
    public ProblemResponse getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
        return ProblemResponse.fromEntity(problem);
    }

    // 4. Create a new problem from ProblemRequest DTO and save to database
    public ProblemResponse createProblem(ProblemRequest request) {
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

        Problem savedProblem = problemRepository.save(problem);
        return ProblemResponse.fromEntity(savedProblem);
    }

    // 5. Update an existing problem, or throw 404 exception if not found
    public ProblemResponse updateProblem(Long id, ProblemRequest request) {
        Problem existingProblem = problemRepository.findById(id)
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

    // 6. Delete a problem by ID, or throw 404 exception if not found
    public void deleteProblem(Long id) {
        if (!problemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Problem not found with id: " + id);
        }
        problemRepository.deleteById(id);
    }

    // 7. Mark a problem as solved: sets status to "Solved", solvedDate to today, revisionCount to 0
    public ProblemResponse markAsSolved(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        problem.setStatus("Solved");
        problem.setSolvedDate(LocalDate.now());
        problem.setRevisionCount(0);

        Problem savedProblem = problemRepository.save(problem);
        return ProblemResponse.fromEntity(savedProblem);
    }

    // 8. Mark a problem as revised: sets lastRevisedDate to today and increments revisionCount by 1
    public ProblemResponse markAsRevised(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        problem.setLastRevisedDate(LocalDate.now());
        problem.setRevisionCount(problem.getRevisionCount() + 1);

        Problem savedProblem = problemRepository.save(problem);
        return ProblemResponse.fromEntity(savedProblem);
    }

    // 9. Helper method: calculates the revision interval in days based on revision count
    public int calculateRevisionInterval(int revisionCount) {
        if (revisionCount <= 0) {
            return 1;
        }
        return switch (revisionCount) {
            case 1 -> 3;
            case 2 -> 7;
            case 3 -> 14;
            default -> 30; // 4 or more revisions
        };
    }

    // 10. Get all solved problems that are due for revision today or earlier
    public List<ProblemRevisionResponse> getProblemsDueForRevision() {
        LocalDate today = LocalDate.now();
        List<Problem> allProblems = problemRepository.findAll();
        List<ProblemRevisionResponse> dueProblems = new ArrayList<>();

        for (Problem problem : allProblems) {
            // Only consider problems that are solved
            if (problem.getStatus() == null || !problem.getStatus().equalsIgnoreCase("Solved")) {
                continue;
            }

            // Base date is lastRevisedDate if present, otherwise solvedDate
            LocalDate baseDate = (problem.getLastRevisedDate() != null)
                    ? problem.getLastRevisedDate()
                    : problem.getSolvedDate();

            // If neither date is set, we cannot calculate next due date
            if (baseDate == null) {
                continue;
            }

            int intervalDays = calculateRevisionInterval(problem.getRevisionCount());
            LocalDate nextRevisionDate = baseDate.plusDays(intervalDays);

            // Problem is due if nextRevisionDate is today or earlier
            if (!nextRevisionDate.isAfter(today)) {
                long daysOverdue = ChronoUnit.DAYS.between(nextRevisionDate, today);
                dueProblems.add(ProblemRevisionResponse.fromEntity(problem, nextRevisionDate, daysOverdue));
            }
        }

        return dueProblems;
    }

    // 11. Get overall preparation stats, topic breakdown, difficulty breakdown, and weakest topic
    public StatsResponse getStats() {
        // Overall problem counts
        long totalProblems = problemRepository.count();
        long solvedCount = problemRepository.countSolvedProblems();
        long pendingCount = totalProblems - solvedCount;

        // Topic breakdown
        List<Object[]> rawTopicStats = problemRepository.countProblemsByTopic();
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
        // Pre-populate with 0s so all three difficulties are always present in the response
        Map<String, DifficultyStats> difficultyBreakdown = new LinkedHashMap<>();
        difficultyBreakdown.put("Easy", new DifficultyStats(0, 0));
        difficultyBreakdown.put("Medium", new DifficultyStats(0, 0));
        difficultyBreakdown.put("Hard", new DifficultyStats(0, 0));

        List<Object[]> rawDifficultyStats = problemRepository.countProblemsByDifficulty();
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

