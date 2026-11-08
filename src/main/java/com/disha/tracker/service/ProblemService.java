package com.disha.tracker.service;

import com.disha.tracker.dto.ProblemRequest;
import com.disha.tracker.dto.ProblemResponse;
import com.disha.tracker.entity.Problem;
import com.disha.tracker.exception.ResourceNotFoundException;
import com.disha.tracker.repository.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
}

