package com.fitnessapp.exercise.app.service;

import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.exercise.app.model.TrackingMethod;
import com.fitnessapp.exercise.app.model.WorkoutStatus;
import com.fitnessapp.exercise.app.model.dto.request.*;
import com.fitnessapp.exercise.app.model.dto.response.WorkoutSessionResponse;
import com.fitnessapp.exercise.app.model.entity.entityNew.*;
import com.fitnessapp.exercise.app.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkoutSessionService {
    private final UserRepo users;
    private final UserWorkoutRepository workouts;
    private final WorkoutExerciseLogRepository exerciseLogs;
    private final WorkoutSetLogRepository setLogs;
    private final ExerciseRepository exercises;
    private final WorkoutPlanRepository plans;

    @Transactional
    public WorkoutSessionResponse start(WorkoutStartRequest request) {
        String userId = currentUserId();
        users.findByIdForUpdate(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Optional<UserWorkout> duplicate = workouts.findByUserIdAndClientRequestId(userId, request.clientRequestId());
        if (duplicate.isPresent()) {
            UserWorkout old = duplicate.get();
            if (old.getSessionMode() != request.sessionMode()
                    || !Objects.equals(old.getWorkoutPlanId(), request.workoutPlanId())
                    || !Objects.equals(old.getPlanDayId(), request.planDayId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "clientRequestId was already used for a different workout");
            }
            return toResponse(old);
        }

        if (request.planDayId() != null && request.workoutPlanId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workoutPlanId is required when planDayId is supplied");
        }

        UserWorkout workout = new UserWorkout();
        workout.setUserId(userId);
        workout.setWorkoutPlanId(request.workoutPlanId());
        workout.setPlanDayId(request.planDayId());
        workout.setSessionMode(request.sessionMode());
        workout.setStatus(WorkoutStatus.IN_PROGRESS);
        workout.setStartedAt(Instant.now());
        workout.setClientRequestId(request.clientRequestId());

        if (request.workoutPlanId() != null) {
            WorkoutPlan plan = plans.findById(request.workoutPlanId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found"));
            if (!plan.isPublished())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Workout plan is not published");
            if (request.planDayId() != null) {
                WorkoutPlanDay day = plan.getDays().stream()
                        .filter(d -> d.getId().equals(request.planDayId())).findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan day does not belong to the selected plan"));
                if (day.isRestDay())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start a workout on a rest day");
                int order = 0;
                for (WorkoutPlanExercise item : day.getPlanExercises()) {
                    WorkoutExerciseLog log = new WorkoutExerciseLog();
                    log.setWorkout(workout);
                    log.setExercise(item.getExercise());
                    log.setSortOrder(order++);
                    log.setStatus("PENDING");
                    log.setTrackingMethod(request.sessionMode() == com.fitnessapp.exercise.app.model.WorkoutSessionMode.AI
                            ? TrackingMethod.AI : TrackingMethod.MANUAL);
                    workout.getExerciseLogs().add(log);
                }
            }
        }

        return toResponse(workouts.save(workout));
    }

    @Transactional
    public WorkoutSessionResponse pause(Long id) {
        UserWorkout workout = ownedWorkout(id);
        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only an active workout can be paused");
        workout.setStatus(WorkoutStatus.PAUSED);
        workout.setPausedAt(Instant.now());
        return toResponse(workout);
    }

    @Transactional
    public WorkoutSessionResponse resume(Long id) {
        UserWorkout workout = ownedWorkout(id);
        if (workout.getStatus() != WorkoutStatus.PAUSED || workout.getPausedAt() == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Workout is not paused");
        long paused = Math.max(0, Duration.between(workout.getPausedAt(), Instant.now()).getSeconds());
        workout.setTotalPausedSeconds(workout.getTotalPausedSeconds() + paused);
        workout.setPausedAt(null);
        workout.setStatus(WorkoutStatus.IN_PROGRESS);
        return toResponse(workout);
    }

    @Transactional
    public WorkoutSessionResponse addExercise(Long id, WorkoutExerciseAddRequest request) {
        UserWorkout workout = ownedWorkout(id);
        requireActive(workout);
        Exercise exercise = exercises.findById(request.exerciseId())
                .filter(Exercise::isPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Published exercise not found"));
        boolean exists = workout.getExerciseLogs().stream().anyMatch(e -> e.getExercise().getId().equals(exercise.getId()));
        if (exists) throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise is already part of this workout");
        WorkoutExerciseLog log = new WorkoutExerciseLog();
        log.setWorkout(workout);
        log.setExercise(exercise);
        log.setSortOrder(workout.getExerciseLogs().stream().mapToInt(WorkoutExerciseLog::getSortOrder).max().orElse(-1) + 1);
        log.setStatus("PENDING");
        log.setTrackingMethod(request.trackingMethod());
        log.setTrackerVersion(normalize(request.trackerVersion()));
        workout.getExerciseLogs().add(log);
        return toResponse(workout);
    }

    @Transactional
    public WorkoutSessionResponse logSet(Long workoutId, Long exerciseLogId, WorkoutSetLogRequest request) {
        UserWorkout workout = ownedWorkout(workoutId);
        requireActive(workout);
        WorkoutExerciseLog exerciseLog = exerciseLogs.findByIdAndWorkout_Id(exerciseLogId, workoutId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout exercise not found"));

        Optional<WorkoutSetLog> existing = setLogs.findByExerciseLog_IdAndSetNumber(exerciseLogId, request.setNumber());
        if (existing.isPresent()) {
            WorkoutSetLog old = existing.get();
            if (!Objects.equals(old.getRepsCompleted(), request.repsCompleted())
                    || !Objects.equals(old.getWeightKg(), request.weightKg())
                    || !Objects.equals(old.getDurationSeconds(), request.durationSeconds())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Set number already recorded with different values");
            }
            return toResponse(workout);
        }
        if (request.repsCompleted() == null && request.durationSeconds() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide repetitions or duration for the set");

        WorkoutSetLog set = new WorkoutSetLog();
        set.setExerciseLog(exerciseLog);
        set.setSetNumber(request.setNumber());
        set.setRepsCompleted(request.repsCompleted());
        set.setWeightKg(request.weightKg());
        set.setDurationSeconds(request.durationSeconds());
        set.setRestSeconds(request.restSeconds());
        set.setRepConfidence(request.repConfidence());
        set.setTrackingMethod(exerciseLog.getTrackingMethod());
        set.setCompletedAt(Instant.now());
        setLogs.save(set);
        exerciseLog.setStatus("IN_PROGRESS");
        if (exerciseLog.getStartedAt() == null) exerciseLog.setStartedAt(Instant.now());
        return toResponse(workout);
    }

    @Transactional
    public WorkoutSessionResponse complete(Long id) {
        UserWorkout workout = ownedWorkout(id);
        if (workout.getStatus() == WorkoutStatus.COMPLETED) return toResponse(workout);
        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS && workout.getStatus() != WorkoutStatus.PAUSED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Workout cannot be completed in its current state");
        if (workout.getExerciseLogs().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Add at least one exercise before completing the workout");
        if (workout.getExerciseLogs().stream().noneMatch(e -> !e.getSetLogs().isEmpty()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Record at least one exercise set before completing the workout");
        Instant now = Instant.now();
        long pausedSeconds = workout.getTotalPausedSeconds();
        if (workout.getPausedAt() != null)
            pausedSeconds += Math.max(0, Duration.between(workout.getPausedAt(), now).getSeconds());
        long elapsed = Math.max(0, Duration.between(workout.getStartedAt(), now).getSeconds() - pausedSeconds);
        if (elapsed < 1 || elapsed > 86400)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workout duration is invalid");
        workout.setCompletedAt(now);
        workout.setDurationSeconds(Math.toIntExact(elapsed));
        workout.setPausedAt(null);
        workout.setTotalPausedSeconds(pausedSeconds);
        workout.setStatus(WorkoutStatus.COMPLETED);
        workout.getExerciseLogs().forEach(e -> {
            if (!e.getSetLogs().isEmpty()) {
                e.setStatus("COMPLETED");
                e.setCompletedAt(now);
            }
        });
        return toResponse(workouts.save(workout));
    }

    @Transactional
    public WorkoutSessionResponse cancel(Long id) {
        UserWorkout workout = ownedWorkout(id);
        if (workout.getStatus() == WorkoutStatus.COMPLETED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed workout cannot be cancelled");
        workout.setStatus(WorkoutStatus.CANCELLED);
        workout.setCompletedAt(Instant.now());
        workout.setPausedAt(null);
        return toResponse(workout);
    }

    @Transactional(readOnly = true)
    public WorkoutSessionResponse get(Long id) {
        return toResponse(ownedWorkout(id));
    }

    @Transactional(readOnly = true)
    public Page<WorkoutSessionResponse> history(Pageable pageable) {
        return workouts.findAllByUserIdOrderByStartedAtDesc(currentUserId(), pageable).map(this::toResponse);
    }

    private UserWorkout ownedWorkout(Long id) {
        return workouts.findByIdAndUserId(id, currentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout session not found"));
    }

    private void requireActive(UserWorkout workout) {
        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Workout must be active to modify it");
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || "anonymousUser".equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return auth.getName();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private WorkoutSessionResponse toResponse(UserWorkout w) {
        List<WorkoutSessionResponse.ExerciseLog> logs = w.getExerciseLogs().stream()
                .sorted(Comparator.comparingInt(WorkoutExerciseLog::getSortOrder))
                .map(e -> new WorkoutSessionResponse.ExerciseLog(
                        e.getId(), e.getExercise().getId(), e.getExercise().getExerciseKey(), e.getExercise().getName(),
                        e.getSortOrder(), e.getStatus(), e.getTrackingMethod().name(), e.getTrackerVersion(),
                        e.getSetLogs().stream().sorted(Comparator.comparingInt(s -> s.getSetNumber()))
                                .map(s -> new WorkoutSessionResponse.SetLog(s.getId(), s.getSetNumber(), s.getRepsCompleted(),
                                        s.getWeightKg(), s.getDurationSeconds(), s.getRestSeconds(), s.getRepConfidence(), s.getCompletedAt()))
                                .toList()
                )).toList();
        Integer duration = w.getDurationSeconds();
        if (duration == null && w.getStartedAt() != null && w.getStatus() != WorkoutStatus.CANCELLED) {
            long paused = w.getTotalPausedSeconds();
            if (w.getPausedAt() != null)
                paused += Math.max(0, Duration.between(w.getPausedAt(), Instant.now()).getSeconds());
            duration = (int) Math.max(0, Duration.between(w.getStartedAt(), Instant.now()).getSeconds() - paused);
        }
        return new WorkoutSessionResponse(w.getId(), w.getWorkoutPlanId(), w.getPlanDayId(), w.getSessionMode(),
                w.getStatus(), w.getStartedAt(), w.getCompletedAt(), duration, logs);
    }
}
