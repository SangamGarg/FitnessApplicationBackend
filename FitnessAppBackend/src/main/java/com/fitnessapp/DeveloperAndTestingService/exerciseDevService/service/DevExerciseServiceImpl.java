package com.fitnessapp.DeveloperAndTestingService.exerciseDevService.service;

import com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models.DevExerciseCategoryRequestDto;
import com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models.DevExerciseDetailsRequestDto;
import com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models.DevExerciseLevelRequestDto;
import com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models.DevExerciseSubCategoryRequestDto;
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

            String responseMessage = String.format(
                    "Level uploaded successfully!\nCategory: %s\nLevel Id: %s",
                    categoryEntity.getName(), savedLevel.getId()
            );


            return ResponseEntity.ok(responseMessage);
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

            String responseMessage = String.format(
                    "Sub Category uploaded successfully!\nCategory: %s\nLevel : %s\nSub Category Id: %s",
                    levelEntity.getCategory().getName(), levelEntity.getLevel(), savedSubCategory.getId()
            );


            return ResponseEntity.ok(responseMessage);
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

            exerciseDetailRepository.save(exerciseEntity);

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
            return ResponseEntity.status(500).body("Error uploading exercise: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> patchCategory(DevExerciseCategoryRequestDto devExerciseCategoryRequestDto) {
        try {
            ExerciseCategoryEntity category = exerciseCategoryRepository.findByNameIgnoreCase(devExerciseCategoryRequestDto.getName());
            if (category == null) {
                return ResponseEntity.status(404).body("Category not found");
            }

            if (devExerciseCategoryRequestDto.getImageUrl() != null) {
                category.setImageUrl(devExerciseCategoryRequestDto.getImageUrl());
            }

            exerciseCategoryRepository.save(category);
            return ResponseEntity.ok("Category updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating category: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> patchLevel(Long categoryId, DevExerciseLevelRequestDto devExerciseLevelRequestDto) {
        try {
            ExerciseCategoryEntity category = exerciseCategoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                return ResponseEntity.status(404).body("Category not found");
            }

            ExerciseLevelEntity level = exerciseLevelRepository.findByLevelIgnoreCaseAndCategory(devExerciseLevelRequestDto.getLevel(), category);
            if (level == null) {
                return ResponseEntity.status(404).body("Level not found");
            }


            if (devExerciseLevelRequestDto.getLevel() != null) {
                level.setLevel(devExerciseLevelRequestDto.getLevel());
            }

            exerciseLevelRepository.save(level);
            return ResponseEntity.ok("Level updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating level: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> patchSubCategory(Long levelId, DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto) {
        try {
            ExerciseLevelEntity level = exerciseLevelRepository.findById(levelId).orElse(null);
            if (level == null) {
                return ResponseEntity.status(404).body("Level not found");
            }

            ExerciseSubCategoryEntity subCategory = exerciseSubCategoryRepository.findByNameIgnoreCaseAndLevel(devExerciseSubCategoryRequestDto.getName(), level);
            if (subCategory == null) {
                return ResponseEntity.status(404).body("Subcategory not found");
            }

            if (devExerciseSubCategoryRequestDto.getImageUrl() != null) {
                subCategory.setImageUrl(devExerciseSubCategoryRequestDto.getImageUrl());
            }

            exerciseSubCategoryRepository.save(subCategory);


            String responseMessage = String.format(
                    "Sub Category updated successfully!\nCategory: %s\nLevel : %s\nSub Category Id: %s",
                    level.getCategory().getName(), level.getLevel(), subCategory.getId()
            );


            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating subcategory: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> patchExerciseDetail(Long subCategoryId, DevExerciseDetailsRequestDto dto) {
        try {
            ExerciseSubCategoryEntity subCategory = exerciseSubCategoryRepository.findById(subCategoryId).orElse(null);
            if (subCategory == null) {
                return ResponseEntity.status(404).body("Subcategory not found");
            }

            ExerciseDetailEntity exercise = exerciseDetailRepository.findByNameIgnoreCaseAndSubCategory(dto.getName(), subCategory);
            if (exercise == null) {
                return ResponseEntity.status(404).body("Exercise not found");
            }

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
            String category = subCategory.getLevel().getCategory().getName();
            String level = subCategory.getLevel().getLevel();
            String subcategory = subCategory.getName();

            String responseMessage = String.format(
                    "Exercise updated successfully!\nCategory: %s\nLevel: %s\nSubcategory: %s\nExercise: %s",
                    category, level, subcategory, exercise
            );

            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating exercise detail: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteCategory(String name) {
        try {
            ExerciseCategoryEntity category = exerciseCategoryRepository.findByNameIgnoreCase(name);
            if (category == null) {
                return ResponseEntity.status(404).body("Category not found");
            }
            exerciseCategoryRepository.delete(category);
            return ResponseEntity.ok("Category deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting category: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> deleteLevel(Long categoryId, String level) {
        try {
            ExerciseCategoryEntity category = exerciseCategoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                return ResponseEntity.status(404).body("Category not found");
            }

            ExerciseLevelEntity level1 = exerciseLevelRepository.findByLevelIgnoreCaseAndCategory(level, category);
            if (level1 == null) {
                return ResponseEntity.status(404).body("Level not found");
            }
            exerciseLevelRepository.delete(level1);


            String responseMessage = String.format(
                    "Level deleted successfully!\nCategory: %s\nLevel Id: %s",
                    category.getName(), level1.getId()
            );

            return ResponseEntity.ok(responseMessage);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting level: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteSubCategory(Long levelId, String subCategory) {
        try {
            ExerciseLevelEntity level = exerciseLevelRepository.findById(levelId).orElse(null);
            if (level == null) {
                return ResponseEntity.status(404).body("Level not found");
            }

            ExerciseSubCategoryEntity subCategory1 = exerciseSubCategoryRepository.findByNameIgnoreCaseAndLevel(subCategory, level);
            if (subCategory1 == null) {
                return ResponseEntity.status(404).body("Subcategory not found");
            }

            exerciseSubCategoryRepository.delete(subCategory1);


            String responseMessage = String.format(
                    "Sub Category deleted successfully!\nCategory: %s\nLevel : %s\nSub Category Id: %s",
                    level.getCategory().getName(), level.getLevel(), subCategory1.getId()
            );


            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting subcategory: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteExerciseDetail(Long subCategoryId, String exerciseName) {
        try {
            ExerciseSubCategoryEntity subCategory = exerciseSubCategoryRepository.findById(subCategoryId).orElse(null);
            if (subCategory == null) {
                return ResponseEntity.status(404).body("Subcategory not found");
            }

            ExerciseDetailEntity exercise = exerciseDetailRepository.findByNameIgnoreCaseAndSubCategory(exerciseName, subCategory);
            if (exercise == null) {
                return ResponseEntity.status(404).body("Exercise not found");
            }

            exerciseDetailRepository.delete(exercise);
            String category = subCategory.getLevel().getCategory().getName();
            String level = subCategory.getLevel().getLevel();
            String subcategory = subCategory.getName();

            String responseMessage = String.format(
                    "Exercise deleted successfully!\nCategory: %s\nLevel: %s\nSubcategory: %s\nExercise: %s",
                    category, level, subcategory, exercise
            );

            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting exercise detail: " + e.getMessage());
        }
    }

}

