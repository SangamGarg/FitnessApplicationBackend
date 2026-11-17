package com.fitnessapp.exercise.repo;

import com.fitnessapp.exercise.model.enums.ExerciseDifficulty;
import com.fitnessapp.exercise.model.enums.ExerciseType;
import com.fitnessapp.exercise.model.entity.exerciseEntity.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    boolean existsByExerciseKeyIgnoreCase(String exerciseKey);

    Optional<Exercise> findByExerciseKeyIgnoreCase(String exerciseKey);

    long countByPublishedTrue();

    List<Exercise> findTop6ByPublishedTrueOrderByCreatedAtDesc();

    interface MuscleGroupCount {
        String getMuscleKey();

        String getName();

        Long getExerciseCount();
    }

    @Query("""
            select mg.muscleKey as muscleKey, mg.name as name, count(distinct e.id) as exerciseCount
            from ExerciseMuscle em
            join em.exerciseMuscleGroup mg
            join em.exercise e
            where e.published = true
            group by mg.muscleKey, mg.name
            order by mg.name
            """)
    List<MuscleGroupCount> countPublishedByMuscleGroup();

    @Query(
            value = """
                    select e from Exercise e
                    where (:published is null or e.published = :published)
                      and (:difficulty is null or e.difficulty = :difficulty)
                      and (:type is null or e.exerciseType = :type)
                      and (
                           :query = ''
                           or lower(e.name) like concat('%', :query, '%')
                           or lower(e.exerciseKey) like concat('%', :query, '%')
                      )
                    """,
            countQuery = """
                    select count(e) from Exercise e
                    where (:published is null or e.published = :published)
                      and (:difficulty is null or e.difficulty = :difficulty)
                      and (:type is null or e.exerciseType = :type)
                      and (
                           :query = ''
                           or lower(e.name) like concat('%', :query, '%')
                           or lower(e.exerciseKey) like concat('%', :query, '%')
                      )
                    """
    )
    Page<Exercise> search(
            @Param("query") String query,
            @Param("difficulty") ExerciseDifficulty difficulty,
            @Param("type") ExerciseType type,
            @Param("published") Boolean published,
            Pageable pageable
    );

    @Query(
            value = """
                    select e from Exercise e
                    where (:published is null or e.published = :published)
                      and exists (
                           select 1 from ExerciseMuscle em
                           join em.exerciseMuscleGroup mg
                           where em.exercise = e
                             and (lower(mg.muscleKey) = :muscle or lower(mg.name) = :muscle)
                             and (:role = '' or upper(em.role) = :role)
                      )
                    """,
            countQuery = """
                    select count(e) from Exercise e
                    where (:published is null or e.published = :published)
                      and exists (
                           select 1 from ExerciseMuscle em
                           join em.exerciseMuscleGroup mg
                           where em.exercise = e
                             and (lower(mg.muscleKey) = :muscle or lower(mg.name) = :muscle)
                             and (:role = '' or upper(em.role) = :role)
                      )
                    """
    )
    Page<Exercise> searchByMuscleGroup(
            @Param("muscle") String muscle,
            @Param("role") String role,
            @Param("published") Boolean published,
            Pageable pageable
    );
}