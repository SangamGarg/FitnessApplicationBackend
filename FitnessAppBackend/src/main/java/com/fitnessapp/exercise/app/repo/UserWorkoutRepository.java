package com.fitnessapp.exercise.app.repo;

import com.fitnessapp.exercise.app.model.entity.entityNew.UserWorkout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserWorkoutRepository extends JpaRepository<UserWorkout, Long> {
    Optional<UserWorkout> findByIdAndUserId(Long id, String userId);
    Optional<UserWorkout> findByUserIdAndClientRequestId(String userId, UUID clientRequestId);
    Page<UserWorkout> findAllByUserIdOrderByStartedAtDesc(String userId, Pageable pageable);
}
