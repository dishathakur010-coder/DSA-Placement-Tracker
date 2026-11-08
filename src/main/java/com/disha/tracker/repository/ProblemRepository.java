package com.disha.tracker.repository;

import com.disha.tracker.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("SELECT p FROM Problem p WHERE " +
           "(:topic IS NULL OR LOWER(p.topic) = LOWER(:topic)) AND " +
           "(:difficulty IS NULL OR LOWER(p.difficulty) = LOWER(:difficulty)) AND " +
           "(:status IS NULL OR LOWER(p.status) = LOWER(:status))")
    List<Problem> findByFilters(
            @Param("topic") String topic,
            @Param("difficulty") String difficulty,
            @Param("status") String status
    );

    @Query("SELECT COUNT(p) FROM Problem p WHERE LOWER(p.status) = 'solved'")
    long countSolvedProblems();

    @Query("SELECT p.topic, COUNT(p), SUM(CASE WHEN LOWER(p.status) = 'solved' THEN 1L ELSE 0L END) " +
           "FROM Problem p WHERE p.topic IS NOT NULL GROUP BY p.topic")
    List<Object[]> countProblemsByTopic();

    @Query("SELECT p.difficulty, COUNT(p), SUM(CASE WHEN LOWER(p.status) = 'solved' THEN 1L ELSE 0L END) " +
           "FROM Problem p WHERE p.difficulty IS NOT NULL GROUP BY p.difficulty")
    List<Object[]> countProblemsByDifficulty();
}


