package com.disha.tracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String topic;
    private String difficulty;
    private String link;
    private String status;
    private String notes;

    private LocalDate solvedDate;
    private LocalDate lastRevisedDate;
    private int revisionCount = 0;

    // Default no-argument constructor (required by JPA)
    public Problem() {
    }

    // Constructor with basic fields (useful for manual object creation)
    public Problem(String title, String topic, String difficulty, String link, String status, String notes) {
        this.title = title;
        this.topic = topic;
        this.difficulty = difficulty;
        this.link = link;
        this.status = status;
        this.notes = notes;
    }

    // Constructor with all fields
    public Problem(String title, String topic, String difficulty, String link, String status, String notes, LocalDate solvedDate, LocalDate lastRevisedDate, int revisionCount) {
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


