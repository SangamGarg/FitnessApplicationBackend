package com.fitnessapp.portal.dev.exerciseDev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fitnessapp.portal.dev.exerciseDev.models.DevExerciseCategoryRequestDto;
import com.fitnessapp.portal.dev.exerciseDev.models.DevExerciseDetailsRequestDto;
import com.fitnessapp.portal.dev.exerciseDev.models.DevExerciseLevelRequestDto;
import com.fitnessapp.portal.dev.exerciseDev.models.DevExerciseSubCategoryRequestDto;
import com.fitnessapp.fitnessExercises.models.entities.ExerciseCategoryEntity;
import com.fitnessapp.fitnessExercises.models.entities.ExerciseDetailEntity;
import com.fitnessapp.fitnessExercises.models.entities.ExerciseLevelEntity;
import com.fitnessapp.fitnessExercises.models.entities.ExerciseSubCategoryEntity;
import com.fitnessapp.fitnessExercises.repositories.ExerciseCategoryRepository;
import com.fitnessapp.fitnessExercises.repositories.ExerciseDetailsRepository;
import com.fitnessapp.fitnessExercises.repositories.ExerciseLevelRepository;
import com.fitnessapp.fitnessExercises.repositories.ExerciseSubCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DevExerciseServiceImpl implements DevExerciseService {

    private final ExerciseCategoryRepository exerciseCategoryRepository;
    private final ExerciseLevelRepository exerciseLevelRepository;
    private final ExerciseSubCategoryRepository exerciseSubCategoryRepository;
    private final ExerciseDetailsRepository exerciseDetailRepository;

    // Upload category
    @Override
    @Transactional
    public ResponseEntity<?> uploadCategory(DevExerciseCategoryRequestDto devExerciseCategoryRequestDto) {
        try {
            log.info("Upload exercise category name={}", devExerciseCategoryRequestDto.getName());
            ExerciseCategoryEntity categoryEntity = new ExerciseCategoryEntity();
            categoryEntity.setName(devExerciseCategoryRequestDto.getName());
            categoryEntity.setImageUrl(devExerciseCategoryRequestDto.getImageUrl());

            ExerciseCategoryEntity savedCategory = exerciseCategoryRepository.save(categoryEntity);
            log.info("Exercise category created categoryId={}", savedCategory.getId());

            return ResponseEntity.ok("Category uploaded successfully! Category ID: " + savedCategory.getId());
        } catch (Exception e) {
            log.error("Failed to upload exercise category", e);
            return ResponseEntity.status(500).body("Unable to upload category");
        }
    }

    // Upload level for a category
    @Override
    @Transactional
    public ResponseEntity<?> uploadLevel(Long categoryId, DevExerciseLevelRequestDto devExerciseLevelRequestDto) {
        try {
            log.info("Upload exercise level categoryId={}", categoryId);
            ExerciseCategoryEntity categoryEntity = exerciseCategoryRepository.findById(categoryId).orElse(null);
            if (categoryEntity == null) {
                log.warn("Category not found categoryId={}", categoryId);
                return ResponseEntity.status(404).body("Category not found");
            }

            ExerciseLevelEntity levelEntity = new ExerciseLevelEntity();
            levelEntity.setLevel(devExerciseLevelRequestDto.getLevel());
            levelEntity.setCategory(categoryEntity);

            ExerciseLevelEntity savedLevel = exerciseLevelRepository.save(levelEntity);
            log.info("Exercise level created levelId={} categoryId={}", savedLevel.getId(), categoryId);

            String responseMessage = String.format(
                    "Level uploaded successfully!\nCategory: %s\nLevel Id: %s",
                    categoryEntity.getName(), savedLevel.getId()
            );


            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            log.error("Failed to upload exercise level categoryId={}", categoryId, e);
            return ResponseEntity.status(500).body("Unable to upload level");
        }
    }

    // Upload subcategory for a level
    @Override
    @Transactional
    public ResponseEntity<?> uploadSubCategory(Long levelId, DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto) {
        try {
            log.info("Upload exercise subcategory levelId={}", levelId);
            ExerciseLevelEntity levelEntity = exerciseLevelRepository.findById(levelId).orElse(null);
            if (levelEntity == null) {
                log.warn("Exercise level not found levelId={}", levelId);
                return ResponseEntity.status(404).body("Level not found");
            }

            ExerciseSubCategoryEntity subCategoryEntity = new ExerciseSubCategoryEntity();
            subCategoryEntity.setName(devExerciseSubCategoryRequestDto.getName());
            subCategoryEntity.setImageUrl(devExerciseSubCategoryRequestDto.getImageUrl());
            subCategoryEntity.setLevel(levelEntity);

            ExerciseSubCategoryEntity savedSubCategory = exerciseSubCategoryRepository.save(subCategoryEntity);
            log.info("Exercise subcategory created subCategoryId={} levelId={}", savedSubCategory.getId(), levelId);

            String responseMessage = String.format(
                    "Sub Category uploaded successfully!\nCategory: %s\nLevel : %s\nSub Category Id: %s",
                    levelEntity.getCategory().getName(), levelEntity.getLevel(), savedSubCategory.getId()
            );


            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            log.error("Failed to upload exercise subcategory levelId={}", levelId, e);
            return ResponseEntity.status(500).body("Unable to upload subcategory");
        }
    }

    // Upload exercise for a subcategory
    @Override
    @Transactional
    public ResponseEntity<?> uploadExerciseDetail(Long subCategoryId, DevExerciseDetailsRequestDto devExerciseDetailsRequestDto) {
        try {
            log.info("Upload exercise detail subCategoryId={}", subCategoryId);
            ExerciseSubCategoryEntity subCategoryEntity = exerciseSubCategoryRepository.findById(subCategoryId).orElse(null);
            if (subCategoryEntity == null) {
                log.warn("Exercise subcategory not found subCategoryId={}", subCategoryId);
                return ResponseEntity.status(404).body("Subcategory not found");
            }

            ExerciseDetailEntity exerciseEntity = new ExerciseDetailEntity();
            exerciseEntity.setName(devExerciseDetailsRequestDto.getName());
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

            ExerciseDetailEntity savedExercise = exerciseDetailRepository.save(exerciseEntity);
            log.info("Exercise detail created exerciseId={} subCategoryId={}", savedExercise.getId(), subCategoryId);

            String category = subCategoryEntity.getLevel().getCategory().getName();
            String level = subCategoryEntity.getLevel().getLevel();
            String subcategory = subCategoryEntity.getName();
            String exercise = exerciseEntity.getName();

            String responseMessage = String.format(
                    "Exercise uploaded successfully!\nCategory: %s\nLevel: %s\nSubcategory: %s\nExercise: %s",
                    category, level, subcategory, exercise
            );

            return ResponseEntity.ok(responseMessage);

        } catch (Exception e) {
            log.error("Failed to upload exercise detail subCategoryId={}", subCategoryId, e);
            return ResponseEntity.status(500).body("Unable to upload exercise");
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> patchCategory(DevExerciseCategoryRequestDto devExerciseCategoryRequestDto) {
        try {
            log.info("Patch exercise category name={}", devExerciseCategoryRequestDto == null ? null : devExerciseCategoryRequestDto.getName());
            if (devExerciseCategoryRequestDto == null || isBlank(devExerciseCategoryRequestDto.getName())) {
                log.warn("Rejected category patch because category name is missing");
                return ResponseEntity.badRequest().body("Category name is required");
            }
            ExerciseCategoryEntity category = exerciseCategoryRepository.findByNameIgnoreCase(devExerciseCategoryRequestDto.getName());
            if (category == null) {
                log.warn("Exercise category not found name={}", devExerciseCategoryRequestDto.getName());
                return ResponseEntity.status(404).body("Category not found");
            }

            if (devExerciseCategoryRequestDto.getImageUrl() != null) {
                category.setImageUrl(devExerciseCategoryRequestDto.getImageUrl());
            }

            exerciseCategoryRepository.save(category);
            log.info("Exercise category updated categoryId={}", category.getId());
            return ResponseEntity.ok("Category updated successfully");
        } catch (Exception e) {
            log.error("Failed to patch exercise category", e);
            return ResponseEntity.status(500).body("Unable to update category");
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> patchLevel(Long categoryId, DevExerciseLevelRequestDto devExerciseLevelRequestDto) {
        try {
            log.info("Patch exercise level categoryId={}", categoryId);
            if (devExerciseLevelRequestDto == null || isBlank(devExerciseLevelRequestDto.getLevel())) {
                log.warn("Rejected level patch because level name is missing categoryId={}", categoryId);
                return ResponseEntity.badRequest().body("Level is required");
            }
            ExerciseCategoryEntity category = exerciseCategoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                log.warn("Exercise category not found categoryId={}", categoryId);
                return ResponseEntity.status(404).body("Category not found");
            }

            ExerciseLevelEntity level = exerciseLevelRepository.findByLevelIgnoreCaseAndCategory(devExerciseLevelRequestDto.getLevel(), category);
            if (level == null) {
                log.warn("Exercise level not found categoryId={} level={}", categoryId, devExerciseLevelRequestDto.getLevel());
                return ResponseEntity.status(404).body("Level not found");
            }


            if (devExerciseLevelRequestDto.getLevel() != null) {
                level.setLevel(devExerciseLevelRequestDto.getLevel());
            }

            exerciseLevelRepository.save(level);
            log.info("Exercise level updated levelId={}", level.getId());
            return ResponseEntity.ok("Level updated successfully");
        } catch (Exception e) {
            log.error("Failed to patch exercise level categoryId={}", categoryId, e);
            return ResponseEntity.status(500).body("Unable to update level");
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> patchSubCategory(Long levelId, DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto) {
        try {
            log.info("Patch exercise subcategory levelId={}", levelId);
            if (devExerciseSubCategoryRequestDto == null || isBlank(devExerciseSubCategoryRequestDto.getName())) {
                log.warn("Rejected subcategory patch because name is missing levelId={}", levelId);
                return ResponseEntity.badRequest().body("Subcategory name is required");
            }
            ExerciseLevelEntity level = exerciseLevelRepository.findById(levelId).orElse(null);
            if (level == null) {
                log.warn("Exercise level not found for patch levelId={}", levelId);
                return ResponseEntity.status(404).body("Level not found");
            }

            ExerciseSubCategoryEntity subCategory = exerciseSubCategoryRepository.findByNameIgnoreCaseAndLevel(devExerciseSubCategoryRequestDto.getName(), level);
            if (subCategory == null) {
                log.warn("Exercise subcategory not found levelId={} name={}", levelId, devExerciseSubCategoryRequestDto.getName());
                return ResponseEntity.status(404).body("Subcategory not found");
            }

            if (devExerciseSubCategoryRequestDto.getImageUrl() != null) {
                subCategory.setImageUrl(devExerciseSubCategoryRequestDto.getImageUrl());
            }

            exerciseSubCategoryRepository.save(subCategory);
            log.info("Exercise subcategory updated subCategoryId={}", subCategory.getId());

            String responseMessage = String.format(
                    "Sub Category updated successfully!\nCategory: %s\nLevel : %s\nSub Category Id: %s",
                    level.getCategory().getName(), level.getLevel(), subCategory.getId()
            );


            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            log.error("Failed to patch exercise subcategory levelId={}", levelId, e);
            return ResponseEntity.status(500).body("Unable to update subcategory");
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> patchExerciseDetail(Long subCategoryId, DevExerciseDetailsRequestDto dto) {
        try {
            log.info("Patch exercise detail subCategoryId={}", subCategoryId);
            if (dto == null || isBlank(dto.getName())) {
                log.warn("Rejected exercise patch because exercise name is missing subCategoryId={}", subCategoryId);
                return ResponseEntity.badRequest().body("Exercise name is required");
            }
            ExerciseSubCategoryEntity subCategory = exerciseSubCategoryRepository.findById(subCategoryId).orElse(null);
            if (subCategory == null) {
                log.warn("Exercise subcategory not found subCategoryId={}", subCategoryId);
                return ResponseEntity.status(404).body("Subcategory not found");
            }

            ExerciseDetailEntity exercise = exerciseDetailRepository.findByNameIgnoreCaseAndSubCategory(dto.getName(), subCategory);
            if (exercise == null) {
                log.warn("Exercise not found for patch subCategoryId={}", subCategoryId);
                return ResponseEntity.status(404).body("Exercise not found");
            }

            log.trace("Applying non-null patch fields to exerciseId={}", exercise.getId());
            // Update patchable fields only if non-null
            if (dto.getDescription() != null) exercise.setDescription(dto.getDescription());
            if (dto.getVideoUrl() != null) exercise.setVideoUrl(dto.getVideoUrl());
            if (dto.getImageUrl() != null) exercise.setImageUrl(dto.getImageUrl());
            if (dto.getDurationInSeconds() != null) exercise.setDurationInSeconds(dto.getDurationInSeconds());
            if (dto.getRepetitions() != null) exercise.setRepetitions(dto.getRepetitions());
            if (dto.getSets() != null) exercise.setSets(dto.getSets());
            if (dto.getCategory() != null) exercise.setCategory(dto.getCategory());
            if (dto.getMuscleGroups() != null) exercise.setMuscleGroups(dto.getMuscleGroups());
            if (dto.getEquipment() != null) exercise.setEquipment(dto.getEquipment());
            if (dto.getDifficulty() != null) exercise.setDifficulty(dto.getDifficulty());
            if (dto.getBenefits() != null) exercise.setBenefits(dto.getBenefits());
            if (dto.getInstructions() != null) exercise.setInstructions(dto.getInstructions());
            if (dto.getTips() != null) exercise.setTips(dto.getTips());
            if (dto.getCaloriesBurned() != null) exercise.setCaloriesBurned(dto.getCaloriesBurned());

            exerciseDetailRepository.save(exercise);
            log.info("Exercise detail updated exerciseId={} subCategoryId={}", exercise.getId(), subCategoryId);
            String category = subCategory.getLevel().getCategory().getName();
            String level = subCategory.getLevel().getLevel();
            String subcategory = subCategory.getName();

            String responseMessage = String.format(
                    "Exercise updated successfully!\nCategory: %s\nLevel: %s\nSubcategory: %s\nExercise: %s",
                    category, level, subcategory, exercise.getName()
            );

            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            log.error("Failed to patch exercise detail subCategoryId={}", subCategoryId, e);
            return ResponseEntity.status(500).body("Unable to update exercise");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteCategory(String name) {
        try {
            log.info("Delete exercise category name={}", name);
            ExerciseCategoryEntity category = exerciseCategoryRepository.findByNameIgnoreCase(name);
            if (category == null) {
                log.warn("Exercise category not found categoryId={}", category.getId());
                return ResponseEntity.status(404).body("Category not found");
            }
            exerciseCategoryRepository.delete(category);
            log.info("Exercise category deleted categoryId={}", category.getId());
            return ResponseEntity.ok("Category deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete exercise category name={}", name, e);
            return ResponseEntity.status(500).body("Unable to delete category");
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> deleteLevel(Long categoryId, String level) {
        try {
            log.info("Delete exercise level categoryId={} level={}", categoryId, level);
            ExerciseCategoryEntity category = exerciseCategoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                log.warn("Exercise category not found categoryId={}", categoryId);
                return ResponseEntity.status(404).body("Category not found");
            }

            ExerciseLevelEntity level1 = exerciseLevelRepository.findByLevelIgnoreCaseAndCategory(level, category);
            if (level1 == null) {
                log.warn("Exercise level not found categoryId={} level={}", categoryId, level);
                return ResponseEntity.status(404).body("Level not found");
            }
            exerciseLevelRepository.delete(level1);
            log.info("Exercise level deleted levelId={} categoryId={}", level1.getId(), categoryId);


            String responseMessage = String.format(
                    "Level deleted successfully!\nCategory: %s\nLevel Id: %s",
                    category.getName(), level1.getId()
            );

            return ResponseEntity.ok(responseMessage);

        } catch (Exception e) {
            log.error("Failed to delete exercise level categoryId={}", categoryId, e);
            return ResponseEntity.status(500).body("Unable to delete level");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteSubCategory(Long levelId, String subCategory) {
        try {
            log.info("Delete exercise subcategory levelId={} name={}", levelId, subCategory);
            ExerciseLevelEntity level = exerciseLevelRepository.findById(levelId).orElse(null);
            if (level == null) {
                log.warn("Exercise level not found levelId={}", levelId);
                return ResponseEntity.status(404).body("Level not found");
            }

            ExerciseSubCategoryEntity subCategory1 = exerciseSubCategoryRepository.findByNameIgnoreCaseAndLevel(subCategory, level);
            if (subCategory1 == null) {
                log.warn("Exercise subcategory not found levelId={} name={}", levelId, subCategory);
                return ResponseEntity.status(404).body("Subcategory not found");
            }

            exerciseSubCategoryRepository.delete(subCategory1);
            log.info("Exercise subcategory deleted subCategoryId={} levelId={}", subCategory1.getId(), levelId);


            String responseMessage = String.format(
                    "Sub Category deleted successfully!\nCategory: %s\nLevel : %s\nSub Category Id: %s",
                    level.getCategory().getName(), level.getLevel(), subCategory1.getId()
            );


            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            log.error("Failed to delete exercise subcategory levelId={}", levelId, e);
            return ResponseEntity.status(500).body("Unable to delete subcategory");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteExerciseDetail(Long subCategoryId, String exerciseName) {
        try {
            log.info("Delete exercise detail subCategoryId={} name={}", subCategoryId, exerciseName);
            ExerciseSubCategoryEntity subCategory = exerciseSubCategoryRepository.findById(subCategoryId).orElse(null);
            if (subCategory == null) {
                log.warn("Exercise subcategory not found subCategoryId={}", subCategoryId);
                return ResponseEntity.status(404).body("Subcategory not found");
            }

            ExerciseDetailEntity exercise = exerciseDetailRepository.findByNameIgnoreCaseAndSubCategory(exerciseName, subCategory);
            if (exercise == null) {
                log.warn("Exercise detail not found subCategoryId={} name={}", subCategoryId, exerciseName);
                return ResponseEntity.status(404).body("Exercise not found");
            }

            exerciseDetailRepository.delete(exercise);
            log.info("Exercise detail deleted exerciseId={} subCategoryId={}", exercise.getId(), subCategoryId);
            String category = subCategory.getLevel().getCategory().getName();
            String level = subCategory.getLevel().getLevel();
            String subcategory = subCategory.getName();

            String responseMessage = String.format(
                    "Exercise deleted successfully!\nCategory: %s\nLevel: %s\nSubcategory: %s\nExercise: %s",
                    category, level, subcategory, exercise.getName()
            );

            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            log.error("Failed to delete exercise detail subCategoryId={}", subCategoryId, e);
            return ResponseEntity.status(500).body("Unable to delete exercise");
        }
    }


    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}

