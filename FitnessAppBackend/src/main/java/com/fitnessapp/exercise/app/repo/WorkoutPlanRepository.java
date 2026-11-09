
package com.fitnessapp.exercise.app.repo;

import com.fitnessapp.exercise.app.model.entity.entityNew.WorkoutPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    Optional<WorkoutPlan> findByPlanKeyIgnoreCase(String planKey);

    boolean existsByPlanKeyIgnoreCase(String planKey);

    @Query(
            value = """
            SELECT p FROM WorkoutPlan p
            WHERE (:published IS NULL OR p.published = :published)
              AND (:goal = '' OR LOWER(p.goal) = LOWER(:goal))
              AND (:difficulty = '' OR LOWER(p.difficulty) = LOWER(:difficulty))
              AND (
                    :query = ''
                    OR LOWER(p.name) LIKE CONCAT('%', LOWER(:query), '%')
                    OR LOWER(p.planKey) LIKE CONCAT('%', LOWER(:query), '%')
              )
            ORDER BY p.name
            """,
            countQuery = """
            SELECT COUNT(p) FROM WorkoutPlan p
            WHERE (:published IS NULL OR p.published = :published)
              AND (:goal = '' OR LOWER(p.goal) = LOWER(:goal))
              AND (:difficulty = '' OR LOWER(p.difficulty) = LOWER(:difficulty))
              AND (
                    :query = ''
                    OR LOWER(p.name) LIKE CONCAT('%', LOWER(:query), '%')
                    OR LOWER(p.planKey) LIKE CONCAT('%', LOWER(:query), '%')
              )
            """
    )
    Page<WorkoutPlan> search(
            @Param("query") String query,
            @Param("goal") String goal,
            @Param("difficulty") String difficulty,
            @Param("published") Boolean published,
            Pageable pageable
    );
}