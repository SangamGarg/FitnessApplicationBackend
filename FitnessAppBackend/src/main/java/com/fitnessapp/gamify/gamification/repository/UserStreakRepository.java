package com.fitnessapp.gamify.gamification.repository;

import com.fitnessapp.gamify.gamification.model.entity.UserStreakEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreakEntity, Long> {
    Optional<UserStreakEntity> findByUserId(String userId);
}
