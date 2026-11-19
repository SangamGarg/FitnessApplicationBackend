package com.fitnessapp.exercise.repo;

import com.fitnessapp.exercise.model.entity.UserWorkout;
import com.fitnessapp.exercise.model.enums.WorkoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserWorkoutRepository extends JpaRepository<UserWorkout, Long> {
    Optional<UserWorkout> findByIdAndUserId(Long id, String userId);
    Optional<UserWorkout> findByUserIdAndClientRequestId(String userId, UUID clientRequestId);
    Page<UserWorkout> findAllByUserIdOrderByStartedAtDesc(String userId, Pageable pageable);

    Optional<UserWorkout> findFirstByUserIdAndStatusInOrderByStartedAtDesc(String userId, Collection<WorkoutStatus> statuses);

    List<UserWorkout> findTop3ByUserIdAndStatusOrderByStartedAtDesc(String userId, WorkoutStatus status);

    boolean existsByUserIdAndStatusIn(String userId, Collection<WorkoutStatus> statuses);

    long countByUserIdAndStatus(String userId, WorkoutStatus status);

    long countByUserIdAndStatusAndCompletedAtGreaterThanEqual(String userId, WorkoutStatus status, Instant completedAfter);
}