package com.disha.tracker.dto;

import java.util.List;
import java.util.Map;

public class StatsResponse {

    private long totalProblems;
    private long solvedCount;
    private long pendingCount;
    private List<TopicStats> topicBreakdown;
    private Map<String, DifficultyStats> difficultyBreakdown;
    private String weakestTopic;

    // Default constructor (useful for JSON serialization)
    public StatsResponse() {
    }

    // All-arguments constructor
    public StatsResponse(long totalProblems, long solvedCount, long pendingCount,
                         List<TopicStats> topicBreakdown,
                         Map<String, DifficultyStats> difficultyBreakdown,
                         String weakestTopic) {
        this.totalProblems = totalProblems;
        this.solvedCount = solvedCount;
        this.pendingCount = pendingCount;
        this.topicBreakdown = topicBreakdown;
        this.difficultyBreakdown = difficultyBreakdown;
        this.weakestTopic = weakestTopic;
    }

    // Getters and Setters
    public long getTotalProblems() {
        return totalProblems;
    }

    public void setTotalProblems(long totalProblems) {
        this.totalProblems = totalProblems;
    }

    public long getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(long solvedCount) {
        this.solvedCount = solvedCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public List<TopicStats> getTopicBreakdown() {
        return topicBreakdown;
    }

    public void setTopicBreakdown(List<TopicStats> topicBreakdown) {
        this.topicBreakdown = topicBreakdown;
    }

    public Map<String, DifficultyStats> getDifficultyBreakdown() {
        return difficultyBreakdown;
    }

    public void setDifficultyBreakdown(Map<String, DifficultyStats> difficultyBreakdown) {
        this.difficultyBreakdown = difficultyBreakdown;
    }

    public String getWeakestTopic() {
        return weakestTopic;
    }

    public void setWeakestTopic(String weakestTopic) {
        this.weakestTopic = weakestTopic;
    }
}

