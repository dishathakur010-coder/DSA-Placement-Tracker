package com.disha.tracker.controller;

import com.disha.tracker.dto.ProblemResponse;
import com.disha.tracker.dto.TopicSummaryDto;
import com.disha.tracker.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Topics", description = "Topic cards, summary metrics, and topic-specific problem listing")
@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    // GET /api/topics/summary - Topic cards summary
    @Operation(summary = "Get summary metrics for all solved topics, sorted by most overdue first")
    @GetMapping("/summary")
    public ResponseEntity<List<TopicSummaryDto>> getTopicsSummary() {
        List<TopicSummaryDto> summaries = topicService.getTopicsSummary();
        return ResponseEntity.ok(summaries);
    }

    // GET /api/topics/problems?topic=<name> - Topic problems listing
    @Operation(summary = "Get problems for a specific topic with suggested problem highlighted first")
    @GetMapping("/problems")
    public ResponseEntity<List<ProblemResponse>> getTopicProblems(@RequestParam("topic") String topic) {
        List<ProblemResponse> problems = topicService.getTopicProblems(topic);
        return ResponseEntity.ok(problems);
    }
}

