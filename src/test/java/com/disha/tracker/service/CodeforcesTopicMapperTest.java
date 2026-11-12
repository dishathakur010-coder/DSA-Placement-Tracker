package com.disha.tracker.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeforcesTopicMapperTest {

    @Test
    @DisplayName("Difficulty mapping correctly maps ratings to Easy, Medium, Hard")
    void testDifficultyMapping() {
        assertEquals("Easy", CodeforcesTopicMapper.mapDifficulty(800));
        assertEquals("Easy", CodeforcesTopicMapper.mapDifficulty(1200));

        assertEquals("Medium", CodeforcesTopicMapper.mapDifficulty(1201));
        assertEquals("Medium", CodeforcesTopicMapper.mapDifficulty(1500));
        assertEquals("Medium", CodeforcesTopicMapper.mapDifficulty(1700));

        assertEquals("Hard", CodeforcesTopicMapper.mapDifficulty(1701));
        assertEquals("Hard", CodeforcesTopicMapper.mapDifficulty(2400));
        assertEquals("Hard", CodeforcesTopicMapper.mapDifficulty(3500));

        // Unrated / null rating defaults to Medium
        assertEquals("Medium", CodeforcesTopicMapper.mapDifficulty(null));
    }

    @Test
    @DisplayName("Topic mapping maps Codeforces tags to DSA topics")
    void testTopicMapping() {
        assertEquals("Dynamic Programming", CodeforcesTopicMapper.mapTopic(List.of("dp")));
        assertEquals("Graphs", CodeforcesTopicMapper.mapTopic(List.of("dfs and similar")));
        assertEquals("Graphs", CodeforcesTopicMapper.mapTopic(List.of("graphs")));
        assertEquals("Trees", CodeforcesTopicMapper.mapTopic(List.of("trees")));
        assertEquals("Binary Search", CodeforcesTopicMapper.mapTopic(List.of("binary search")));
        assertEquals("Two Pointers", CodeforcesTopicMapper.mapTopic(List.of("two pointers")));
        assertEquals("Strings", CodeforcesTopicMapper.mapTopic(List.of("strings")));
        assertEquals("Greedy", CodeforcesTopicMapper.mapTopic(List.of("greedy")));
        assertEquals("Math", CodeforcesTopicMapper.mapTopic(List.of("math")));
        assertEquals("Heap", CodeforcesTopicMapper.mapTopic(List.of("heap")));
        assertEquals("Arrays", CodeforcesTopicMapper.mapTopic(List.of("sortings")));
        assertEquals("Recursion/Backtracking", CodeforcesTopicMapper.mapTopic(List.of("divide and conquer")));

        // Unmapped tags fall back to Other
        assertEquals("Other", CodeforcesTopicMapper.mapTopic(List.of("interactive")));
        assertEquals("Other", CodeforcesTopicMapper.mapTopic(Collections.emptyList()));
        assertEquals("Other", CodeforcesTopicMapper.mapTopic(null));
    }

    @Test
    @DisplayName("Topic mapping picks first recognized tag when problem has multiple tags")
    void testMultipleTags() {
        assertEquals("Dynamic Programming", CodeforcesTopicMapper.mapTopic(List.of("dp", "trees", "math")));
        assertEquals("Trees", CodeforcesTopicMapper.mapTopic(List.of("trees", "dp")));
    }
}

