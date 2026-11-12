package com.disha.tracker.dto;

import java.util.List;

public class TopicInsightsResponse {

    private List<TopicInsightDto> topics;
    private String mostNeglectedTopic;
    private Long mostNeglectedDays;
    private String strongestTopic;
    private Long strongestTopicSolved;

    public TopicInsightsResponse() {
    }

    public TopicInsightsResponse(List<TopicInsightDto> topics, String mostNeglectedTopic,
                                 Long mostNeglectedDays, String strongestTopic,
                                 Long strongestTopicSolved) {
        this.topics = topics;
        this.mostNeglectedTopic = mostNeglectedTopic;
        this.mostNeglectedDays = mostNeglectedDays;
        this.strongestTopic = strongestTopic;
        this.strongestTopicSolved = strongestTopicSolved;
    }

    public List<TopicInsightDto> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicInsightDto> topics) {
        this.topics = topics;
    }

    public String getMostNeglectedTopic() {
        return mostNeglectedTopic;
    }

    public void setMostNeglectedTopic(String mostNeglectedTopic) {
        this.mostNeglectedTopic = mostNeglectedTopic;
    }

    public Long getMostNeglectedDays() {
        return mostNeglectedDays;
    }

    public void setMostNeglectedDays(Long mostNeglectedDays) {
        this.mostNeglectedDays = mostNeglectedDays;
    }

    public String getStrongestTopic() {
        return strongestTopic;
    }

    public void setStrongestTopic(String strongestTopic) {
        this.strongestTopic = strongestTopic;
    }

    public Long getStrongestTopicSolved() {
        return strongestTopicSolved;
    }

    public void setStrongestTopicSolved(Long strongestTopicSolved) {
        this.strongestTopicSolved = strongestTopicSolved;
    }
}

