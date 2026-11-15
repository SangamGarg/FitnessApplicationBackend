package com.fitnessapp.gamify.challenge.repository;

import com.fitnessapp.gamify.challenge.model.entity.ChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<ChallengeEntity, Long> {
    List<ChallengeEntity> findAllByActiveTrueAndStartsAtLessThanEqualAndEndsAtAfterOrderByEndsAtAsc(Instant now, Instant now2);
}
