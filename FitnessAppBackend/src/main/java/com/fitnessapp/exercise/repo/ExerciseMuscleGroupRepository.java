package com.fitnessapp.exercise.repo;

import com.fitnessapp.exercise.model.entity.exerciseEntity.ExerciseMuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ExerciseMuscleGroupRepository extends JpaRepository<ExerciseMuscleGroup, Long> {
    boolean existsByMuscleKeyIgnoreCase(String muscleKey);

    @Query("select case when count(muscle) > 0 then true else false end "
            + "from ExerciseMuscle muscle where muscle.exerciseMuscleGroup.id = :muscleGroupId")
    boolean isAssignedToExercise(@Param("muscleGroupId") Long muscleGroupId);

    @Query("select muscleGroup from ExerciseMuscleGroup muscleGroup "
            + "where lower(muscleGroup.muscleKey) in :muscleKeys")
    List<ExerciseMuscleGroup> findByMuscleKeysIgnoreCase(@Param("muscleKeys") Collection<String> muscleKeys);
}
