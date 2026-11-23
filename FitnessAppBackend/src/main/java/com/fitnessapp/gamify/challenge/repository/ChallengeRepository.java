package com.fitnessapp.gamify.challenge.repository;

import com.fitnessapp.gamify.challenge.model.entity.ChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity, Long> {
    List<ChallengeEntity> findAllByOrderByStartsAtDesc();

    List<ChallengeEntity> findAllByActiveTrueAndStartsAtLessThanEqualAndEndsAtAfterOrderByEndsAtAsc(Instant now, Instant now2);
}
