package com.disha.tracker.service;

import com.disha.tracker.entity.Problem;
import com.disha.tracker.repository.ProblemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    // Get all problems
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    // Get a single problem by ID
    public Optional<Problem> getProblemById(Long id) {
        return problemRepository.findById(id);
    }

    // Save a new problem
    public Problem createProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    // Update an existing problem
    public Optional<Problem> updateProblem(Long id, Problem updatedProblem) {
        return problemRepository.findById(id).map(existingProblem -> {
            existingProblem.setTitle(updatedProblem.getTitle());
            existingProblem.setTopic(updatedProblem.getTopic());
            existingProblem.setDifficulty(updatedProblem.getDifficulty());
            existingProblem.setLink(updatedProblem.getLink());
            existingProblem.setStatus(updatedProblem.getStatus());
            existingProblem.setNotes(updatedProblem.getNotes());
            return problemRepository.save(existingProblem);
        });
    }

    // Delete a problem by ID
    public boolean deleteProblem(Long id) {
        if (problemRepository.existsById(id)) {
            problemRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

