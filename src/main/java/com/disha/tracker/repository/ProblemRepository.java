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
}


