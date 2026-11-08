package com.fitnessapp.gamification.challenge;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserChallengeRepository extends JpaRepository<UserChallengeEntity,Long>{ Optional<UserChallengeEntity> findByUserIdAndChallengeId(String userId,Long challengeId); List<UserChallengeEntity> findAllByUserIdOrderByJoinedAtDesc(String userId); List<UserChallengeEntity> findAllByUserIdAndStatus(String userId,ChallengeStatus status); }
