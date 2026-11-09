package com.disha.tracker.controller;

import com.disha.tracker.dto.StatsResponse;
import com.disha.tracker.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Stats", description = "Preparation statistics and analytics")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final ProblemService problemService;

    public StatsController(ProblemService problemService) {
        this.problemService = problemService;
    }

    // GET /api/stats - returns overall stats, topic breakdown, difficulty breakdown, and weakest topic
    @Operation(summary = "Get preparation statistics and topic breakdown")
    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        StatsResponse stats = problemService.getStats();
        return ResponseEntity.ok(stats);
    }
}

