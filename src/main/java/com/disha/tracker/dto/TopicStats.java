package com.disha.tracker.dto;

public class TopicStats {

    private String topic;
    private long total;
    private long solved;
    private double solvedPercentage;

    // Default constructor (useful for JSON serialization)
    public TopicStats() {
    }

    // All-arguments constructor
    public TopicStats(String topic, long total, long solved, double solvedPercentage) {
        this.topic = topic;
        this.total = total;
        this.solved = solved;
        this.solvedPercentage = solvedPercentage;
    }

    // Getters and Setters
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getSolved() {
        return solved;
    }

    public void setSolved(long solved) {
        this.solved = solved;
    }

    public double getSolvedPercentage() {
        return solvedPercentage;
    }

    public void setSolvedPercentage(double solvedPercentage) {
        this.solvedPercentage = solvedPercentage;
    }
}

