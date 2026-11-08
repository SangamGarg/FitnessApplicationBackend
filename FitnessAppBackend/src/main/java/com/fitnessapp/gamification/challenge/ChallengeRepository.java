package com.fitnessapp.gamification.challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant; import java.util.List;
public interface ChallengeRepository extends JpaRepository<ChallengeEntity,Long>{ List<ChallengeEntity> findAllByActiveTrueAndStartsAtLessThanEqualAndEndsAtAfterOrderByEndsAtAsc(Instant now,Instant now2); }
