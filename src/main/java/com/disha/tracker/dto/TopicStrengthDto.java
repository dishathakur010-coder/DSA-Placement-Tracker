package com.disha.tracker.dto;

public class TopicStrengthDto {

    private String topic;
    private double strengthScore;
    private String strengthLabel;
    private long totalSolved;

    public TopicStrengthDto() {
    }

    public TopicStrengthDto(String topic, double strengthScore, String strengthLabel, long totalSolved) {
        this.topic = topic;
        this.strengthScore = strengthScore;
        this.strengthLabel = strengthLabel;
        this.totalSolved = totalSolved;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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

    public long getTotalSolved() {
        return totalSolved;
    }

    public void setTotalSolved(long totalSolved) {
        this.totalSolved = totalSolved;
    }
}

