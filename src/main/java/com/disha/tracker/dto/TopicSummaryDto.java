package com.disha.tracker.dto;

import java.time.LocalDate;

public class TopicSummaryDto {

    private String topic;
    private long totalSolved;
    private long revisedCount;
    private long readyToReviseCount;
    private long masteredCount;
    private LocalDate lastSolvedDate;
    private LocalDate lastRevisedDate;
    private long daysSinceLastActivity;
    private double strengthScore;
    private String strengthLabel;
    private ProblemResponse suggestedProblem;

    public TopicSummaryDto() {
    }

    public TopicSummaryDto(String topic, long totalSolved, long revisedCount,
                           long readyToReviseCount, long masteredCount,
                           LocalDate lastSolvedDate, LocalDate lastRevisedDate,
                           long daysSinceLastActivity, double strengthScore,
                           String strengthLabel, ProblemResponse suggestedProblem) {
        this.topic = topic;
        this.totalSolved = totalSolved;
        this.revisedCount = revisedCount;
        this.readyToReviseCount = readyToReviseCount;
        this.masteredCount = masteredCount;
        this.lastSolvedDate = lastSolvedDate;
        this.lastRevisedDate = lastRevisedDate;
        this.daysSinceLastActivity = daysSinceLastActivity;
        this.strengthScore = strengthScore;
        this.strengthLabel = strengthLabel;
        this.suggestedProblem = suggestedProblem;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getTotalSolved() {
        return totalSolved;
    }

    public void setTotalSolved(long totalSolved) {
        this.totalSolved = totalSolved;
    }

    public long getRevisedCount() {
        return revisedCount;
    }

    public void setRevisedCount(long revisedCount) {
        this.revisedCount = revisedCount;
    }

    public long getReadyToReviseCount() {
        return readyToReviseCount;
    }

    public void setReadyToReviseCount(long readyToReviseCount) {
        this.readyToReviseCount = readyToReviseCount;
    }

    public long getMasteredCount() {
        return masteredCount;
    }

    public void setMasteredCount(long masteredCount) {
        this.masteredCount = masteredCount;
    }

    public LocalDate getLastSolvedDate() {
        return lastSolvedDate;
    }

    public void setLastSolvedDate(LocalDate lastSolvedDate) {
        this.lastSolvedDate = lastSolvedDate;
    }

    public LocalDate getLastRevisedDate() {
        return lastRevisedDate;
    }

    public void setLastRevisedDate(LocalDate lastRevisedDate) {
        this.lastRevisedDate = lastRevisedDate;
    }

    public long getDaysSinceLastActivity() {
        return daysSinceLastActivity;
    }

    public void setDaysSinceLastActivity(long daysSinceLastActivity) {
        this.daysSinceLastActivity = daysSinceLastActivity;
    }

    public double getStrengthScore() {
        return strengthScore;
    }

    public void setStrengthScore(double strengthScore) {
        this.strengthScore = strengthScore;
    }

    public String getStrengthLabel() {
        return strengthLabel;
    }

    public void setStrengthLabel(String strengthLabel) {
        this.strengthLabel = strengthLabel;
    }

    public ProblemResponse getSuggestedProblem() {
        return suggestedProblem;
    }

    public void setSuggestedProblem(ProblemResponse suggestedProblem) {
        this.suggestedProblem = suggestedProblem;
    }
}

