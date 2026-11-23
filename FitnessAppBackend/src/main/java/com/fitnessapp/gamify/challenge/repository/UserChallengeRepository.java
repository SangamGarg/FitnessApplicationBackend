package com.fitnessapp.gamify.challenge.repository;

import com.fitnessapp.gamify.challenge.model.enums.ChallengeStatus;
import com.fitnessapp.gamify.challenge.model.entity.UserChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallengeEntity, Long> {
    Optional<UserChallengeEntity> findByUserIdAndChallengeId(String userId, Long challengeId);

    List<UserChallengeEntity> findAllByUserIdOrderByJoinedAtDesc(String userId);

    List<UserChallengeEntity> findAllByUserIdAndStatus(String userId, ChallengeStatus status);
}
