package com.disha.tracker.dto;

import com.disha.tracker.entity.Problem;
import com.disha.tracker.service.RevisionRules;

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
    private String platform;
    private String externalId;

    // Revision Lock and Mastery Metadata
    private boolean canRevise;
    private long daysUntilRevisable;
    private boolean mastered;
    private boolean suggested;

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
        ProblemResponse response = new ProblemResponse(
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
        response.setPlatform(problem.getPlatform());
        response.setExternalId(problem.getExternalId());

        LocalDate today = LocalDate.now();
        response.setMastered(RevisionRules.isMastered(problem));
        response.setCanRevise(RevisionRules.canRevise(problem, today));
        response.setDaysUntilRevisable(RevisionRules.daysUntilRevisable(problem, today));
        response.setSuggested(false);

        return response;
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public boolean isCanRevise() {
        return canRevise;
    }

    public void setCanRevise(boolean canRevise) {
        this.canRevise = canRevise;
    }

    public long getDaysUntilRevisable() {
        return daysUntilRevisable;
    }

    public void setDaysUntilRevisable(long daysUntilRevisable) {
        this.daysUntilRevisable = daysUntilRevisable;
    }

    public boolean isMastered() {
        return mastered;
    }

    public void setMastered(boolean mastered) {
        this.mastered = mastered;
    }

    public boolean isSuggested() {
        return suggested;
    }

    public void setSuggested(boolean suggested) {
        this.suggested = suggested;
    }
}
