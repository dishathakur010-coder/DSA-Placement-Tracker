package com.disha.tracker.controller;

import com.disha.tracker.dto.RevisionStatsResponse;
import com.disha.tracker.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Revision Stats", description = "Separate revision statistics and topic strength analytics")
@RestController
@RequestMapping("/api/revision")
public class RevisionStatsController {

    private final TopicService topicService;

    public RevisionStatsController(TopicService topicService) {
        this.topicService = topicService;
    }

    // GET /api/revision/stats - Overall revision statistics and per-topic strength
    @Operation(summary = "Get overall revision counts and per-topic strength metrics")
    @GetMapping("/stats")
    public ResponseEntity<RevisionStatsResponse> getRevisionStats() {
        RevisionStatsResponse response = topicService.getRevisionStats();
        return ResponseEntity.ok(response);
    }
}

