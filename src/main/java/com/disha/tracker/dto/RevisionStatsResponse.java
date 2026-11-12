package com.disha.tracker.dto;

import java.util.List;

public class RevisionStatsResponse {

    private long totalRevisionsDone;
    private long problemsRevisedAtLeastOnce;
    private long problemsNeverRevised;
    private long problemsMastered;
    private double averageRevisionsPerProblem;
    private long revisionsLast7Days;
    private List<TopicStrengthDto> topicStrengths;
    private String strongestTopic;
    private String weakestTopic;

    public RevisionStatsResponse() {
    }

    public RevisionStatsResponse(long totalRevisionsDone, long problemsRevisedAtLeastOnce,
                                 long problemsNeverRevised, long problemsMastered,
                                 double averageRevisionsPerProblem, long revisionsLast7Days,
                                 List<TopicStrengthDto> topicStrengths, String strongestTopic,
                                 String weakestTopic) {
        this.totalRevisionsDone = totalRevisionsDone;
        this.problemsRevisedAtLeastOnce = problemsRevisedAtLeastOnce;
        this.problemsNeverRevised = problemsNeverRevised;
        this.problemsMastered = problemsMastered;
        this.averageRevisionsPerProblem = averageRevisionsPerProblem;
        this.revisionsLast7Days = revisionsLast7Days;
        this.topicStrengths = topicStrengths;
        this.strongestTopic = strongestTopic;
        this.weakestTopic = weakestTopic;
    }

    public long getTotalRevisionsDone() {
        return totalRevisionsDone;
    }

    public void setTotalRevisionsDone(long totalRevisionsDone) {
        this.totalRevisionsDone = totalRevisionsDone;
    }

    public long getProblemsRevisedAtLeastOnce() {
        return problemsRevisedAtLeastOnce;
    }

    public void setProblemsRevisedAtLeastOnce(long problemsRevisedAtLeastOnce) {
        this.problemsRevisedAtLeastOnce = problemsRevisedAtLeastOnce;
    }

    public long getProblemsNeverRevised() {
        return problemsNeverRevised;
    }

    public void setProblemsNeverRevised(long problemsNeverRevised) {
        this.problemsNeverRevised = problemsNeverRevised;
    }

    public long getProblemsMastered() {
        return problemsMastered;
    }

    public void setProblemsMastered(long problemsMastered) {
        this.problemsMastered = problemsMastered;
    }

    public double getAverageRevisionsPerProblem() {
        return averageRevisionsPerProblem;
    }

    public void setAverageRevisionsPerProblem(double averageRevisionsPerProblem) {
        this.averageRevisionsPerProblem = averageRevisionsPerProblem;
    }

    public long getRevisionsLast7Days() {
        return revisionsLast7Days;
    }

    public void setRevisionsLast7Days(long revisionsLast7Days) {
        this.revisionsLast7Days = revisionsLast7Days;
    }

    public List<TopicStrengthDto> getTopicStrengths() {
        return topicStrengths;
    }

    public void setTopicStrengths(List<TopicStrengthDto> topicStrengths) {
        this.topicStrengths = topicStrengths;
    }

    public String getStrongestTopic() {
        return strongestTopic;
    }

    public void setStrongestTopic(String strongestTopic) {
        this.strongestTopic = strongestTopic;
    }

    public String getWeakestTopic() {
        return weakestTopic;
    }

    public void setWeakestTopic(String weakestTopic) {
        this.weakestTopic = weakestTopic;
    }
}

