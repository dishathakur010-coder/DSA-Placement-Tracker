package com.disha.tracker.repository;

import com.disha.tracker.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    // Find a problem by ID only if it belongs to the specified user
    Optional<Problem> findByIdAndUserId(Long id, Long userId);

    // Find all problems belonging to a user (used for revise-today)
    List<Problem> findByUserId(Long userId);

    // Count total problems belonging to a user
    long countByUserId(Long userId);

    // Filter problems belonging to a user by optional topic, difficulty, and status
    @Query("SELECT p FROM Problem p WHERE p.user.id = :userId AND " +
           "(:topic IS NULL OR LOWER(p.topic) = LOWER(:topic)) AND " +
           "(:difficulty IS NULL OR LOWER(p.difficulty) = LOWER(:difficulty)) AND " +
           "(:status IS NULL OR LOWER(p.status) = LOWER(:status))")
    List<Problem> findByUserAndFilters(
            @Param("userId") Long userId,
            @Param("topic") String topic,
            @Param("difficulty") String difficulty,
            @Param("status") String status
    );

    // Count solved problems for a user
    @Query("SELECT COUNT(p) FROM Problem p WHERE p.user.id = :userId AND LOWER(p.status) = 'solved'")
    long countSolvedProblemsByUser(@Param("userId") Long userId);

    // Count problems grouped by topic for a user
    @Query("SELECT p.topic, COUNT(p), SUM(CASE WHEN LOWER(p.status) = 'solved' THEN 1L ELSE 0L END) " +
           "FROM Problem p WHERE p.user.id = :userId AND p.topic IS NOT NULL GROUP BY p.topic")
    List<Object[]> countProblemsByTopicForUser(@Param("userId") Long userId);

    // Count problems grouped by difficulty for a user
    @Query("SELECT p.difficulty, COUNT(p), SUM(CASE WHEN LOWER(p.status) = 'solved' THEN 1L ELSE 0L END) " +
           "FROM Problem p WHERE p.user.id = :userId AND p.difficulty IS NOT NULL GROUP BY p.difficulty")
    List<Object[]> countProblemsByDifficultyForUser(@Param("userId") Long userId);
}
