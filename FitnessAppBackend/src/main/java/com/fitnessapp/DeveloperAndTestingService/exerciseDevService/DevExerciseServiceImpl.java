package com.fitnessapp.DeveloperAndTestingService.exerciseDevService;

import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseCategoryEntity;
import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseDetailEntity;
import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseLevelEntity;
import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseSubCategoryEntity;
import com.fitnessapp.fitnessExercisesService.repositories.ExerciseCategoryRepository;
import com.fitnessapp.fitnessExercisesService.repositories.ExerciseDetailsRepository;
import com.fitnessapp.fitnessExercisesService.repositories.ExerciseLevelRepository;
import com.fitnessapp.fitnessExercisesService.repositories.ExerciseSubCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DevExerciseServiceImpl implements DevExerciseService {

    private final ExerciseCategoryRepository exerciseCategoryRepository;
    private final ExerciseLevelRepository exerciseLevelRepository;
    private final ExerciseSubCategoryRepository exerciseSubCategoryRepository;
    private final ExerciseDetailsRepository exerciseDetailRepository;

    public DevExerciseServiceImpl(ExerciseCategoryRepository exerciseCategoryRepository, ExerciseLevelRepository exerciseLevelRepository, ExerciseSubCategoryRepository exerciseSubCategoryRepository, ExerciseDetailsRepository exerciseDetailRepository) {
        this.exerciseCategoryRepository = exerciseCategoryRepository;
        this.exerciseLevelRepository = exerciseLevelRepository;
        this.exerciseSubCategoryRepository = exerciseSubCategoryRepository;
        this.exerciseDetailRepository = exerciseDetailRepository;
    }

    // Upload category
    @Override
    @Transactional
    public ResponseEntity<?> uploadCategory(DevExerciseCategoryRequestDto devExerciseCategoryRequestDto) {
        try {
            ExerciseCategoryEntity categoryEntity = new ExerciseCategoryEntity();
            categoryEntity.setName(devExerciseCategoryRequestDto.getName());
            categoryEntity.setImageUrl(devExerciseCategoryRequestDto.getImageUrl());

            ExerciseCategoryEntity savedCategory = exerciseCategoryRepository.save(categoryEntity);

            return ResponseEntity.ok("Category uploaded successfully! Category ID: " + savedCategory.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading category: " + e.getMessage());
        }
    }

    // Upload level for a category
    @Override
    @Transactional
    public ResponseEntity<?> uploadLevel(Long categoryId, DevExerciseLevelRequestDto devExerciseLevelRequestDto) {
        try {
            ExerciseCategoryEntity categoryEntity = exerciseCategoryRepository.findById(categoryId).orElse(null);
            if (categoryEntity == null) {
                return ResponseEntity.status(404).body("Category not found");
            }

            ExerciseLevelEntity levelEntity = new ExerciseLevelEntity();
            levelEntity.setLevel(devExerciseLevelRequestDto.getLevel());
            levelEntity.setCategory(categoryEntity);

            ExerciseLevelEntity savedLevel = exerciseLevelRepository.save(levelEntity);

            return ResponseEntity.ok("Level uploaded successfully! Level ID: " + savedLevel.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading level: " + e.getMessage());
        }
    }

    // Upload subcategory for a level
    @Override
    @Transactional
    public ResponseEntity<?> uploadSubCategory(Long levelId, DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto) {
        try {
            ExerciseLevelEntity levelEntity = exerciseLevelRepository.findById(levelId).orElse(null);
            if (levelEntity == null) {
                return ResponseEntity.status(404).body("Level not found");
            }

            ExerciseSubCategoryEntity subCategoryEntity = new ExerciseSubCategoryEntity();
            subCategoryEntity.setName(devExerciseSubCategoryRequestDto.getName());
            subCategoryEntity.setImageUrl(devExerciseSubCategoryRequestDto.getImageUrl());
            subCategoryEntity.setLevel(levelEntity);

            ExerciseSubCategoryEntity savedSubCategory = exerciseSubCategoryRepository.save(subCategoryEntity);

            return ResponseEntity.ok("Subcategory uploaded successfully! Subcategory ID: " + savedSubCategory.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading subcategory: " + e.getMessage());
        }
    }

    // Upload exercise for a subcategory
    @Override
    @Transactional
    public ResponseEntity<?> uploadExerciseDetail(Long subCategoryId, DevExerciseDetailsRequestDto devExerciseDetailsRequestDto) {
        try {
            ExerciseSubCategoryEntity subCategoryEntity = exerciseSubCategoryRepository.findById(subCategoryId).orElse(null);
            if (subCategoryEntity == null) {
                return ResponseEntity.status(404).body("Subcategory not found");
            }

            ExerciseDetailEntity exerciseEntity = new ExerciseDetailEntity();
            exerciseEntity.setTitle(devExerciseDetailsRequestDto.getTitle());
            exerciseEntity.setDescription(devExerciseDetailsRequestDto.getDescription());
            exerciseEntity.setVideoUrl(devExerciseDetailsRequestDto.getVideoUrl());
            exerciseEntity.setImageUrl(devExerciseDetailsRequestDto.getImageUrl());
            exerciseEntity.setDurationInSeconds(devExerciseDetailsRequestDto.getDurationInSeconds());
            exerciseEntity.setRepetitions(devExerciseDetailsRequestDto.getRepetitions());
            exerciseEntity.setSets(devExerciseDetailsRequestDto.getSets());
            exerciseEntity.setCategory(devExerciseDetailsRequestDto.getCategory());
            exerciseEntity.setMuscleGroups(devExerciseDetailsRequestDto.getMuscleGroups());
            exerciseEntity.setEquipment(devExerciseDetailsRequestDto.getEquipment());
            exerciseEntity.setDifficulty(devExerciseDetailsRequestDto.getDifficulty());
            exerciseEntity.setBenefits(devExerciseDetailsRequestDto.getBenefits());
            exerciseEntity.setInstructions(devExerciseDetailsRequestDto.getInstructions());
            exerciseEntity.setTips(devExerciseDetailsRequestDto.getTips());
            exerciseEntity.setCaloriesBurned(devExerciseDetailsRequestDto.getCaloriesBurned());
            exerciseEntity.setSubCategory(subCategoryEntity);

            exerciseDetailRepository.save(exerciseEntity);

            return ResponseEntity.ok("Exercise uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading exercise: " + e.getMessage());
        }
    }
}

