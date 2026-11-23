package com.fitnessapp.gamify.gamification.repository;

import com.fitnessapp.gamify.gamification.model.entity.UserXpAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserXpAccountRepository extends JpaRepository<UserXpAccountEntity, Long> {
    Optional<UserXpAccountEntity> findByUserId(String userId);
}
