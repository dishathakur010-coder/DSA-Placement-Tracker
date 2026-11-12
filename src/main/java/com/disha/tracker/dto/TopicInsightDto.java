package com.disha.tracker.dto;

import java.time.LocalDate;

public class TopicInsightDto {

    private String topic;
    private long totalSolved;
    private long solvedLast30Days;
    private LocalDate lastSolvedDate;
    private Long daysSinceLastSolved;
    private LocalDate lastRevisedDate;
    private long daysSinceLastActivity;
    private String revisionStatus; // "FRESH", "DUE_SOON", "OVERDUE"

    public TopicInsightDto() {
    }

    public TopicInsightDto(String topic, long totalSolved, long solvedLast30Days,
                           LocalDate lastSolvedDate, Long daysSinceLastSolved,
                           LocalDate lastRevisedDate, long daysSinceLastActivity,
                           String revisionStatus) {
        this.topic = topic;
        this.totalSolved = totalSolved;
        this.solvedLast30Days = solvedLast30Days;
        this.lastSolvedDate = lastSolvedDate;
        this.daysSinceLastSolved = daysSinceLastSolved;
        this.lastRevisedDate = lastRevisedDate;
        this.daysSinceLastActivity = daysSinceLastActivity;
        this.revisionStatus = revisionStatus;
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

    public long getSolvedLast30Days() {
        return solvedLast30Days;
    }

    public void setSolvedLast30Days(long solvedLast30Days) {
        this.solvedLast30Days = solvedLast30Days;
    }

    public LocalDate getLastSolvedDate() {
        return lastSolvedDate;
    }

    public void setLastSolvedDate(LocalDate lastSolvedDate) {
        this.lastSolvedDate = lastSolvedDate;
    }

    public Long getDaysSinceLastSolved() {
        return daysSinceLastSolved;
    }

    public void setDaysSinceLastSolved(Long daysSinceLastSolved) {
        this.daysSinceLastSolved = daysSinceLastSolved;
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

    public String getRevisionStatus() {
        return revisionStatus;
    }

    public void setRevisionStatus(String revisionStatus) {
        this.revisionStatus = revisionStatus;
    }
}

