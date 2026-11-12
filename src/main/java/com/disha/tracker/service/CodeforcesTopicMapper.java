package com.disha.tracker.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Small readable mapper that converts Codeforces ratings into difficulty levels
 * (Easy, Medium, Hard) and Codeforces tags into the tracker's predefined DSA topics.
 */
public final class CodeforcesTopicMapper {

    private static final Map<String, String> TAG_TO_TOPIC_MAP = new HashMap<>();

    static {
        // Dynamic Programming
        TAG_TO_TOPIC_MAP.put("dp", "Dynamic Programming");

        // Trees
        TAG_TO_TOPIC_MAP.put("trees", "Trees");

        // Graphs
        TAG_TO_TOPIC_MAP.put("graphs", "Graphs");
        TAG_TO_TOPIC_MAP.put("dfs and similar", "Graphs");
        TAG_TO_TOPIC_MAP.put("shortest paths", "Graphs");
        TAG_TO_TOPIC_MAP.put("flows", "Graphs");
        TAG_TO_TOPIC_MAP.put("graph matchings", "Graphs");
        TAG_TO_TOPIC_MAP.put("dsu", "Graphs");

        // Binary Search
        TAG_TO_TOPIC_MAP.put("binary search", "Binary Search");
        TAG_TO_TOPIC_MAP.put("ternary search", "Binary Search");

        // Two Pointers
        TAG_TO_TOPIC_MAP.put("two pointers", "Two Pointers");

        // Strings
        TAG_TO_TOPIC_MAP.put("strings", "Strings");
        TAG_TO_TOPIC_MAP.put("string suffix structures", "Strings");

        // Hashing
        TAG_TO_TOPIC_MAP.put("hashing", "Hashing");

        // Greedy
        TAG_TO_TOPIC_MAP.put("greedy", "Greedy");

        // Math
        TAG_TO_TOPIC_MAP.put("math", "Math");
        TAG_TO_TOPIC_MAP.put("number theory", "Math");
        TAG_TO_TOPIC_MAP.put("combinatorics", "Math");
        TAG_TO_TOPIC_MAP.put("geometry", "Math");
        TAG_TO_TOPIC_MAP.put("probabilities", "Math");
        TAG_TO_TOPIC_MAP.put("matrices", "Math");

        // Heap / Priority Queue
        TAG_TO_TOPIC_MAP.put("heap", "Heap");
        TAG_TO_TOPIC_MAP.put("priority queue", "Heap");

        // Recursion / Backtracking
        TAG_TO_TOPIC_MAP.put("divide and conquer", "Recursion/Backtracking");

        // Arrays
        TAG_TO_TOPIC_MAP.put("sortings", "Arrays");
    }

    private CodeforcesTopicMapper() {
        // Utility class, prevent instantiation
    }

    /**
     * Map Codeforces rating to difficulty level:
     * - Up to 1200 -> Easy
     * - Up to 1700 -> Medium
     * - Above 1700 -> Hard
     * - No rating (null) -> Medium
     */
    public static String mapDifficulty(Integer rating) {
        if (rating == null) {
            return "Medium";
        }
        if (rating <= 1200) {
            return "Easy";
        }
        if (rating <= 1700) {
            return "Medium";
        }
        return "Hard";
    }

    /**
     * Map Codeforces tags to existing topic list.
     * Checks each tag in order and returns the first recognized topic,
     * or "Other" if none match.
     */
    public static String mapTopic(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "Other";
        }

        for (String rawTag : tags) {
            if (rawTag == null) {
                continue;
            }
            String normalizedTag = rawTag.trim().toLowerCase();
            String matchedTopic = TAG_TO_TOPIC_MAP.get(normalizedTag);
            if (matchedTopic != null) {
                return matchedTopic;
            }
        }

        return "Other";
    }
}

