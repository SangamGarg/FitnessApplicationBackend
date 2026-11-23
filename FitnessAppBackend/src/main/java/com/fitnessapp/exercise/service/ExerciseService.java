package com.fitnessapp.exercise.service;

import com.fitnessapp.exercise.model.dto.request.WorkoutPlanUpsertRequestDto;
import com.fitnessapp.exercise.model.dto.request.CreateExerciseMuscleGroupRequestDto;
import com.fitnessapp.exercise.model.dto.request.UpdateExerciseMuscleGroupRequestDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseHomeResponseDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseMuscleGroupResponseDto;
import com.fitnessapp.exercise.model.dto.response.WorkoutPlanResponseDto;
import com.fitnessapp.exercise.model.enums.ExerciseDifficulty;
import com.fitnessapp.exercise.model.enums.ExerciseType;
import com.fitnessapp.exercise.model.dto.response.ExerciseResponseDto;
import com.fitnessapp.exercise.model.dto.request.ExerciseUpsertRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ExerciseService {
    ResponseEntity<ExerciseHomeResponseDto> getExerciseHome();

    List<ExerciseMuscleGroupResponseDto> getExerciseMuscleGroups();

    ExerciseMuscleGroupResponseDto createExerciseMuscleGroup(CreateExerciseMuscleGroupRequestDto request);

    ExerciseMuscleGroupResponseDto updateExerciseMuscleGroup(
            Long id, UpdateExerciseMuscleGroupRequestDto request);

    void deleteExerciseMuscleGroup(Long id);

    Page<ExerciseResponseDto> getPublishedExercisesList(
            String query,
            ExerciseDifficulty difficulty,
            ExerciseType type,
            Boolean published,
            Pageable pageable
    );

    Page<ExerciseResponseDto> getPublishedExercisesList(
            String query,
            ExerciseDifficulty difficulty,
            ExerciseType type,
            String muscle,
            String role,
            Boolean published,
            Pageable pageable
    );

    ExerciseResponseDto getPublishedExercise(Long id, boolean publishedOnly);

    Page<ExerciseResponseDto> searchExercisesByMuscleGroup(
            String muscle,
            String role,
            Boolean published,
            Pageable pageable
    );


    ExerciseResponseDto createExercise(ExerciseUpsertRequestDto request, List<MultipartFile> mediaFiles);


    ExerciseResponseDto updateExercise(Long id, ExerciseUpsertRequestDto request, List<MultipartFile> mediaFiles);

    ExerciseResponseDto setExercisePublished(Long id, boolean published);

    void deleteExercise(Long id);

    /*
     * WorkOut PLans
     * */
    Page<WorkoutPlanResponseDto> getPublishedWorkoutPlansList(
            String query,
            String goal,
            String difficulty,
            Boolean published,
            Pageable pageable
    );

    WorkoutPlanResponseDto getPublishedWorkoutPlan(Long id, boolean publishedOnly);

    WorkoutPlanResponseDto createWorkoutPlan(WorkoutPlanUpsertRequestDto request);

    WorkoutPlanResponseDto updateWorkoutPlan(Long id, WorkoutPlanUpsertRequestDto request);

    WorkoutPlanResponseDto setWorkoutPlanPublished(Long id, boolean published);

    void deleteWorkoutPlan(Long id);

}