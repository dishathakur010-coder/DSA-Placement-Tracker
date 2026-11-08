package com.disha.tracker.controller;

import com.disha.tracker.dto.ProblemRequest;
import com.disha.tracker.dto.ProblemResponse;
import com.disha.tracker.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    // 1. GET problems (with optional filters): GET /api/problems?topic=Arrays&difficulty=Easy&status=Solved
    @GetMapping
    public List<ProblemResponse> getProblems(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String status) {
        return problemService.getProblems(topic, difficulty, status);
    }

    // 2. GET a single problem: GET /api/problems/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponse> getProblemById(@PathVariable Long id) {
        ProblemResponse problem = problemService.getProblemById(id);
        return ResponseEntity.ok(problem);
    }

    // 3. CREATE a new problem: POST /api/problems
    @PostMapping
    public ResponseEntity<ProblemResponse> createProblem(@Valid @RequestBody ProblemRequest request) {
        ProblemResponse createdProblem = problemService.createProblem(request);
        return new ResponseEntity<>(createdProblem, HttpStatus.CREATED);
    }

    // 4. UPDATE a problem: PUT /api/problems/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProblemResponse> updateProblem(@PathVariable Long id, @Valid @RequestBody ProblemRequest request) {
        ProblemResponse updatedProblem = problemService.updateProblem(id, request);
        return ResponseEntity.ok(updatedProblem);
    }

    // 5. DELETE a problem: DELETE /api/problems/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.noContent().build();
    }

    // 6. Mark problem as solved: PATCH /api/problems/{id}/solve
    @PatchMapping("/{id}/solve")
    public ResponseEntity<ProblemResponse> solveProblem(@PathVariable Long id) {
        ProblemResponse response = problemService.markAsSolved(id);
        return ResponseEntity.ok(response);
    }

    // 7. Mark problem as revised: PATCH /api/problems/{id}/revise
    @PatchMapping("/{id}/revise")
    public ResponseEntity<ProblemResponse> reviseProblem(@PathVariable Long id) {
        ProblemResponse response = problemService.markAsRevised(id);
        return ResponseEntity.ok(response);
    }
}

