package com.disha.tracker.controller;

import com.disha.tracker.dto.ProblemRequest;
import com.disha.tracker.dto.ProblemResponse;
import com.disha.tracker.dto.ProblemRevisionResponse;
import com.disha.tracker.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Problems", description = "DSA problem management and spaced revision tracking")
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    // 1. GET problems (with optional filters): GET /api/problems?topic=Arrays&difficulty=Easy&status=Solved
    @Operation(summary = "Get problems with optional topic, difficulty, or status filters")
    @GetMapping
    public List<ProblemResponse> getProblems(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String status) {
        return problemService.getProblems(topic, difficulty, status);
    }

    // 2. GET problems due for revision today: GET /api/problems/revise-today
    @Operation(summary = "Get problems due for revision today")
    @GetMapping("/revise-today")
    public List<ProblemRevisionResponse> getProblemsDueForRevision() {
        return problemService.getProblemsDueForRevision();
    }

    // 3. GET a single problem: GET /api/problems/{id}
    @Operation(summary = "Get a problem by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponse> getProblemById(@PathVariable Long id) {
        ProblemResponse problem = problemService.getProblemById(id);
        return ResponseEntity.ok(problem);
    }

    // 4. CREATE a new problem: POST /api/problems
    @Operation(summary = "Create a new problem")
    @PostMapping
    public ResponseEntity<ProblemResponse> createProblem(@Valid @RequestBody ProblemRequest request) {
        ProblemResponse createdProblem = problemService.createProblem(request);
        return new ResponseEntity<>(createdProblem, HttpStatus.CREATED);
    }

    // 5. UPDATE a problem: PUT /api/problems/{id}
    @Operation(summary = "Update an existing problem by ID")
    @PutMapping("/{id}")
    public ResponseEntity<ProblemResponse> updateProblem(@PathVariable Long id, @Valid @RequestBody ProblemRequest request) {
        ProblemResponse updatedProblem = problemService.updateProblem(id, request);
        return ResponseEntity.ok(updatedProblem);
    }

    // 6. DELETE a problem: DELETE /api/problems/{id}
    @Operation(summary = "Delete a problem by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.noContent().build();
    }

    // 7. Mark problem as solved: PATCH /api/problems/{id}/solve
    @Operation(summary = "Mark a problem as solved")
    @PatchMapping("/{id}/solve")
    public ResponseEntity<ProblemResponse> solveProblem(@PathVariable Long id) {
        ProblemResponse response = problemService.markAsSolved(id);
        return ResponseEntity.ok(response);
    }

    // 8. Mark problem as revised: PATCH /api/problems/{id}/revise
    @Operation(summary = "Mark a problem as revised")
    @PatchMapping("/{id}/revise")
    public ResponseEntity<ProblemResponse> reviseProblem(@PathVariable Long id) {
        ProblemResponse response = problemService.markAsRevised(id);
        return ResponseEntity.ok(response);
    }
}

