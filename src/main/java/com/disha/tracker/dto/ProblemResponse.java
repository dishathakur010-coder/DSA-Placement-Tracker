package com.disha.tracker.dto;

import com.disha.tracker.entity.Problem;

import java.time.LocalDate;

public class ProblemResponse {

    private Long id;
    private String title;
    private String topic;
    private String difficulty;
    private String link;
    private String status;
    private String notes;
    private LocalDate solvedDate;
    private LocalDate lastRevisedDate;
    private int revisionCount;

    // Default constructor
    public ProblemResponse() {
    }

    // Constructor with basic fields
    public ProblemResponse(Long id, String title, String topic, String difficulty, String link, String status, String notes) {
        this.id = id;
        this.title = title;
        this.topic = topic;
        this.difficulty = difficulty;
        this.link = link;
        this.status = status;
        this.notes = notes;
    }

    // Constructor with all fields
    public ProblemResponse(Long id, String title, String topic, String difficulty, String link, String status, String notes, LocalDate solvedDate, LocalDate lastRevisedDate, int revisionCount) {
        this.id = id;
        this.title = title;
        this.topic = topic;
        this.difficulty = difficulty;
        this.link = link;
        this.status = status;
        this.notes = notes;
        this.solvedDate = solvedDate;
        this.lastRevisedDate = lastRevisedDate;
        this.revisionCount = revisionCount;
    }

    // Static helper method to convert a Problem entity into a ProblemResponse DTO
    public static ProblemResponse fromEntity(Problem problem) {
        if (problem == null) {
            return null;
        }
        return new ProblemResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getTopic(),
                problem.getDifficulty(),
                problem.getLink(),
                problem.getStatus(),
                problem.getNotes(),
                problem.getSolvedDate(),
                problem.getLastRevisedDate(),
                problem.getRevisionCount()
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getSolvedDate() {
        return solvedDate;
    }

    public void setSolvedDate(LocalDate solvedDate) {
        this.solvedDate = solvedDate;
    }

    public LocalDate getLastRevisedDate() {
        return lastRevisedDate;
    }

    public void setLastRevisedDate(LocalDate lastRevisedDate) {
        this.lastRevisedDate = lastRevisedDate;
    }

    public int getRevisionCount() {
        return revisionCount;
    }

    public void setRevisionCount(int revisionCount) {
        this.revisionCount = revisionCount;
    }
}


