package com.fitnessapp.gamify.gamification.repository;

import com.fitnessapp.gamify.gamification.model.entity.UserXpAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserXpAccountRepository extends JpaRepository<UserXpAccountEntity, Long> {
    Optional<UserXpAccountEntity> findByUserId(String userId);
}
