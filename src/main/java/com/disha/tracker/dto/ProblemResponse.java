package com.disha.tracker.dto;

import com.disha.tracker.entity.Problem;

public class ProblemResponse {

    private Long id;
    private String title;
    private String topic;
    private String difficulty;
    private String link;
    private String status;
    private String notes;

    // Default constructor
    public ProblemResponse() {
    }

    // Constructor with all fields
    public ProblemResponse(Long id, String title, String topic, String difficulty, String link, String status, String notes) {
        this.id = id;
        this.title = title;
        this.topic = topic;
        this.difficulty = difficulty;
        this.link = link;
        this.status = status;
        this.notes = notes;
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
                problem.getNotes()
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
}

