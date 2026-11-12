package com.disha.tracker.controller;

import com.disha.tracker.dto.TopicInsightsResponse;
import com.disha.tracker.service.InsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Insights", description = "Topic mastery, revision freshness, and preparation insights")
@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    // GET /api/insights/topics - Returns topic mastery and revision freshness insights
    @Operation(summary = "Get topic-level insights including solve count, activity recency, and neglected topic")
    @GetMapping("/topics")
    public ResponseEntity<TopicInsightsResponse> getTopicInsights() {
        TopicInsightsResponse response = insightsService.getTopicInsights();
        return ResponseEntity.ok(response);
    }
}

