package com.fitnessapp.exercise.portal;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.fitnessapp.exercise.app.model.ExerciseDifficulty;
import com.fitnessapp.exercise.app.model.ExerciseType;
import com.fitnessapp.exercise.app.model.entity.entityNew.Exercise;
import com.fitnessapp.exercise.app.model.entity.entityNew.ExerciseMedia;
import com.fitnessapp.exercise.app.repo.ExerciseRepository;
import com.fitnessapp.exercise.portal.dto.ExerciseResponseDto;
import com.fitnessapp.exercise.portal.dto.ExerciseUpsertRequestDto;
import com.fitnessapp.portal.audit.PortalAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ExerciseCatalogService {

    private final ExerciseRepository exercises;
    private final ObjectMapper objectMapper;
    private final PortalAuditService audit;

    @Transactional(readOnly = true)
    public Page<ExerciseResponseDto> search(
            String query,
            ExerciseDifficulty difficulty,
            ExerciseType type,
            Boolean published,
            Pageable pageable
    ) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return exercises.search(normalizedQuery, difficulty, type, published, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ExerciseResponseDto get(Long id, boolean publishedOnly) {
        Exercise exercise = findExercise(id);
        if (publishedOnly && !exercise.isPublished()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found");
        }
        return toResponse(exercise);
    }

    @Transactional
    public ExerciseResponseDto create(ExerciseUpsertRequestDto request) {
        if (exercises.existsByExerciseKeyIgnoreCase(request.exerciseKey().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise key already exists");
        }
        Exercise exercise = new Exercise();
        apply(exercise, request);
        Exercise saved = exercises.save(exercise);
        audit.record("EXERCISE_CREATED", "EXERCISE", saved.getId().toString(), "Exercise created as draft");
        return toResponse(saved);
    }

    @Transactional
    public ExerciseResponseDto update(Long id, ExerciseUpsertRequestDto request) {
        Exercise exercise = findExercise(id);
        if (exercise.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the exercise before editing it");
        }
        exercises.findByExerciseKeyIgnoreCase(request.exerciseKey().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise key already exists");
                });
        apply(exercise, request);
        Exercise saved = exercises.save(exercise);
        audit.record("EXERCISE_UPDATED", "EXERCISE", id.toString(), "Exercise metadata updated");
        return toResponse(saved);
    }

    @Transactional
    public ExerciseResponseDto setPublished(Long id, boolean published) {
        Exercise exercise = findExercise(id);
        if (published && (exercise.getDescription() == null || exercise.getDescription().isBlank())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A description is required before publication");
        }
        exercise.setPublished(published);
        Exercise saved = exercises.save(exercise);
        audit.record(
                published ? "EXERCISE_PUBLISHED" : "EXERCISE_UNPUBLISHED",
                "EXERCISE",
                id.toString(),
                "Exercise published=" + published
        );
        return toResponse(saved);
    }

    @Transactional
    public void deleteDraft(Long id) {
        Exercise exercise = findExercise(id);
        if (exercise.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the exercise before deleting it");
        }
        exercises.delete(exercise);
        audit.record("EXERCISE_DELETED", "EXERCISE", id.toString(), "Draft exercise deleted");
    }

    private void apply(Exercise exercise, ExerciseUpsertRequestDto request) {
        exercise.setExerciseKey(request.exerciseKey().trim());
        exercise.setName(request.name().trim());
        exercise.setDescription(trimToNull(request.description()));
        exercise.setInstructions(validateJson(request.instructionsJson(), true));
        exercise.setDifficulty(request.difficulty());
        exercise.setExerciseType(request.exerciseType());
        exercise.setMovementPattern(trimToNull(request.movementPattern()));
        exercise.setEquipment(request.equipment().trim());
        exercise.setUnilateral(request.unilateral());
        exercise.setSupportsAiTracking(request.supportsAiTracking());
        exercise.setTrackingConfig(validateJson(request.trackingConfigJson(), false));
    }

    private String validateJson(String raw, boolean arrayRequired) {
        if (raw == null || raw.isBlank()) {
            return arrayRequired ? "[]" : null;
        }

        try {
            JsonNode node = objectMapper.readTree(raw);

            if (node == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid JSON in exercise content"
                );
            }

            if (arrayRequired && !node.isArray()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Instructions must be a JSON array"
                );
            }

            return objectMapper.writeValueAsString(node);

        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid JSON in exercise content",
                    exception
            );
        }
    }

    private Exercise findExercise(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exercise ID must be positive");
        }
        return exercises.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ExerciseResponseDto toResponse(Exercise exercise) {
        var media = exercise.getMedia().stream()
                .sorted(java.util.Comparator.comparingInt(ExerciseMedia::getSortOrder))
                .map(item -> new ExerciseResponseDto.MediaItem(
                        item.getId(),
                        item.getMediaType(),
                        item.getMediaUrl(),
                        item.getThumbnailUrl(),
                        item.getSortOrder(),
                        item.isPrimary()
                ))
                .toList();
        return new ExerciseResponseDto(
                exercise.getId(),
                exercise.getExerciseKey(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getInstructions(),
                exercise.getDifficulty(),
                exercise.getExerciseType(),
                exercise.getMovementPattern(),
                exercise.getEquipment(),
                exercise.isUnilateral(),
                exercise.isSupportsAiTracking(),
                exercise.getTrackingConfig(),
                exercise.isPublished(),
                media,
                exercise.getCreatedAt(),
                exercise.getUpdatedAt()
        );
    }
}
