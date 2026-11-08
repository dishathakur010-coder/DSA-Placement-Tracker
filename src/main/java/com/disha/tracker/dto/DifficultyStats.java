package com.disha.tracker.dto;

public class DifficultyStats {

    private long total;
    private long solved;

    // Default constructor (useful for JSON serialization)
    public DifficultyStats() {
    }

    // All-arguments constructor
    public DifficultyStats(long total, long solved) {
        this.total = total;
        this.solved = solved;
    }

    // Getters and Setters
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
}

