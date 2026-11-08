package com.disha.tracker.dto;

import com.disha.tracker.entity.Problem;

import java.time.LocalDate;

public class ProblemRevisionResponse extends ProblemResponse {

    private LocalDate nextRevisionDate;
    private long daysOverdue;

    // Default constructor (useful for JSON serialization/deserialization)
    public ProblemRevisionResponse() {
        super();
    }

    // Constructor with all fields
    public ProblemRevisionResponse(Long id, String title, String topic, String difficulty, String link,
                                   String status, String notes, LocalDate solvedDate, LocalDate lastRevisedDate,
                                   int revisionCount, LocalDate nextRevisionDate, long daysOverdue) {
        super(id, title, topic, difficulty, link, status, notes, solvedDate, lastRevisedDate, revisionCount);
        this.nextRevisionDate = nextRevisionDate;
        this.daysOverdue = daysOverdue;
    }

    // Helper method to build a ProblemRevisionResponse directly from a Problem entity
    public static ProblemRevisionResponse fromEntity(Problem problem, LocalDate nextRevisionDate, long daysOverdue) {
        if (problem == null) {
            return null;
        }
        return new ProblemRevisionResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getTopic(),
                problem.getDifficulty(),
                problem.getLink(),
                problem.getStatus(),
                problem.getNotes(),
                problem.getSolvedDate(),
                problem.getLastRevisedDate(),
                problem.getRevisionCount(),
                nextRevisionDate,
                daysOverdue
        );
    }

    public LocalDate getNextRevisionDate() {
        return nextRevisionDate;
    }

    public void setNextRevisionDate(LocalDate nextRevisionDate) {
        this.nextRevisionDate = nextRevisionDate;
    }

    public long getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(long daysOverdue) {
        this.daysOverdue = daysOverdue;
    }
}

