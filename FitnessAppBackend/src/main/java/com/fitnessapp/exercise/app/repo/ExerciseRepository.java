package com.fitnessapp.exercise.app.repo;

import com.fitnessapp.exercise.app.model.ExerciseDifficulty;
import com.fitnessapp.exercise.app.model.ExerciseType;
import com.fitnessapp.exercise.app.model.entity.entityNew.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    boolean existsByExerciseKeyIgnoreCase(String exerciseKey);

    Optional<Exercise> findByExerciseKeyIgnoreCase(String exerciseKey);

    @Query(
            value = """
                    select e from Exercise e
                    where (:published is null or e.published = :published)
                      and (:difficulty is null or e.difficulty = :difficulty)
                      and (:type is null or e.exerciseType = :type)
                      and (:query is null
                           or lower(e.name) like concat('%', lower(:query), '%')
                           or lower(e.exerciseKey) like concat('%', lower(:query), '%'))
                    """,
            countQuery = """
                    select count(e) from Exercise e
                    where (:published is null or e.published = :published)
                      and (:difficulty is null or e.difficulty = :difficulty)
                      and (:type is null or e.exerciseType = :type)
                      and (:query is null
                           or lower(e.name) like concat('%', lower(:query), '%')
                           or lower(e.exerciseKey) like concat('%', lower(:query), '%'))
                    """
    )
    Page<Exercise> search(
            @Param("query") String query,
            @Param("difficulty") ExerciseDifficulty difficulty,
            @Param("type") ExerciseType type,
            @Param("published") Boolean published,
            Pageable pageable
    );
}
