package com.fitnessapp.exercise.app.service;

import lombok.extern.slf4j.Slf4j;
import com.fitnessapp.exercise.shared.ExerciseUtils;
import com.fitnessapp.exercise.app.model.dto.response.ExerciseCategoryResponseDto;
import com.fitnessapp.exercise.app.model.dto.response.ExerciseDetailResponseDto;
import com.fitnessapp.exercise.app.model.dto.response.ExerciseLevelResponseDto;
import com.fitnessapp.exercise.app.model.dto.response.ExerciseSubCategoryResponseDto;
import com.fitnessapp.exercise.app.model.entity.ExerciseCategoryEntity;
import com.fitnessapp.exercise.app.model.entity.ExerciseDetailEntity;
import com.fitnessapp.exercise.app.model.entity.ExerciseSubCategoryEntity;
import com.fitnessapp.exercise.app.repo.ExerciseCategoryRepository;
import com.fitnessapp.exercise.app.repo.ExerciseDetailsRepository;
import com.fitnessapp.exercise.app.repo.ExerciseLevelRepository;
import com.fitnessapp.exercise.app.repo.ExerciseSubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ExerciseAppServiceImpl implements ExerciseAppService {

    private final ExerciseCategoryRepository categoryRepository;
    private final ExerciseLevelRepository levelRepository;
    private final ExerciseSubCategoryRepository subCategoryRepository;
    private final ExerciseDetailsRepository exerciseRepository;

    @Autowired
    public ExerciseAppServiceImpl(ExerciseCategoryRepository categoryRepository, ExerciseLevelRepository levelRepository, ExerciseSubCategoryRepository subCategoryRepository, ExerciseDetailsRepository exerciseRepository) {
        this.categoryRepository = categoryRepository;
        this.levelRepository = levelRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public ResponseEntity<?> getExerciseAllCategories() {
        try {
            var categories = categoryRepository.findAll();
            List<ExerciseCategoryResponseDto> dtos = categories.stream().map(c ->
                    ExerciseCategoryResponseDto.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .imageUrl(c.getImageUrl())
                            .build()
            ).toList();


            // return ResponseEntity.ok(mapper.toCategoryDTOs(categories));
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ExerciseUtils.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getExerciseLevelsByCategoryId(Long categoryId) {
        try {
            var levels = levelRepository.findByCategoryId(categoryId);
            List<ExerciseLevelResponseDto> dtos = levels.stream().map(l ->
                    ExerciseLevelResponseDto.builder()
                            .id(l.getId())
                            .level(l.getLevel())
                            .build()
            ).toList();

            return ResponseEntity.ok(dtos);


//            return ResponseEntity.ok(mapper.toLevelDTOs(levels));
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ExerciseUtils.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getExerciseSubCategoriesByLevelId(Long levelId) {
        try {
            var subCategories = subCategoryRepository.findByLevelId(levelId);
            List<ExerciseSubCategoryResponseDto> dtos = subCategories.stream().map(s ->
                    ExerciseSubCategoryResponseDto.builder()
                            .id(s.getId())
                            .name(s.getName())
                            .imageUrl(s.getImageUrl())
                            .build()
            ).toList();

            return ResponseEntity.ok(dtos);
            // return ResponseEntity.ok(mapper.toSubCategoryDTOs(subCategories));
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ExerciseUtils.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getExercisesBySubCategoryId(Long subCategoryId) {
        try {
            var exercises = exerciseRepository.findBySubCategoryId(subCategoryId);
            List<ExerciseDetailResponseDto> dtos = exercises.stream().map(d ->
                    ExerciseDetailResponseDto.builder()
                            .id(d.getId())
                            .name(d.getName())
                            .description(d.getDescription())
                            .videoUrl(d.getVideoUrl())
                            .imageUrl(d.getImageUrl())
                            .durationInSeconds(d.getDurationInSeconds())
                            .repetitions(d.getRepetitions())
                            .sets(d.getSets())
                            .category(d.getCategory())
                            .muscleGroups(d.getMuscleGroups())
                            .equipment(d.getEquipment())
                            .difficulty(d.getDifficulty())
                            .benefits(d.getBenefits())
                            .instructions(d.getInstructions())
                            .tips(d.getTips())
                            .caloriesBurned(d.getCaloriesBurned())
                            .build()
            ).toList();

            return ResponseEntity.ok(dtos);
            // return ResponseEntity.ok(mapper.toExerciseDTOs(exercises));
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ExerciseUtils.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getExerciseAllCategoriesSuggestion(String query) {
        try {
            List<ExerciseCategoryEntity> results = categoryRepository
                    .findByNameStartingWithIgnoreCase(query);

            List<String> names = results.stream()
                    .map(ExerciseCategoryEntity::getName)
                    .toList();

            if (names.isEmpty()) {
                return ResponseEntity.ok(List.of());
            } else {
                return ResponseEntity.ok(names);
            }
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ExerciseUtils.errorDto(e));
        }
    }


    @Override
    public ResponseEntity<?> getExerciseSubCategoriesSuggestion(Long levelId, String query) {
        try {
            var subCategories = subCategoryRepository.findByLevelIdAndNameStartingWithIgnoreCase(levelId, query);

            List<String> names = subCategories.stream()
                    .map(ExerciseSubCategoryEntity::getName)
                    .toList();

            if (names.isEmpty()) {
                return ResponseEntity.ok(List.of());
            } else {
                return ResponseEntity.ok(names);
            }
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ExerciseUtils.errorDto(e));
        }
    }


    @Override
    public ResponseEntity<?> getExercisesDetailSuggestion(Long subCategoryId, String query) {
        try {
            var exercises = exerciseRepository.findBySubCategoryIdAndNameStartingWithIgnoreCase(subCategoryId, query);
            List<String> names = exercises.stream()
                    .map(ExerciseDetailEntity::getName)
                    .toList();
            if (names.isEmpty()) {
                return ResponseEntity.ok(List.of());
            } else {
                return ResponseEntity.ok(names);
            }
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ExerciseUtils.errorDto(e));
        }
    }
}
