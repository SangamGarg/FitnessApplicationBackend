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
                    select p from WorkoutPlan p
                    where (:published is null or p.published = :published)
                      and (:goal is null or lower(p.goal) = lower(:goal))
                      and (:difficulty is null or lower(p.difficulty) = lower(:difficulty))
                      and (:query is null
                           or lower(p.name) like concat('%', lower(:query), '%')
                           or lower(p.planKey) like concat('%', lower(:query), '%'))
                    """,
            countQuery = """
                    select count(p) from WorkoutPlan p
                    where (:published is null or p.published = :published)
                      and (:goal is null or lower(p.goal) = lower(:goal))
                      and (:difficulty is null or lower(p.difficulty) = lower(:difficulty))
                      and (:query is null
                           or lower(p.name) like concat('%', lower(:query), '%')
                           or lower(p.planKey) like concat('%', lower(:query), '%'))
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
