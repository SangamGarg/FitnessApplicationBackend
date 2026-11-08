package com.fitnessapp.gamification.game.repo;

import com.fitnessapp.gamification.game.model.GameSessionEntity;
import com.fitnessapp.gamification.game.model.GameSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSessionEntity, UUID> {
    long countByUserIdAndStatus(String userId, GameSessionStatus status);

    @Query("select coalesce(sum(g.repsDetected),0) from GameSessionEntity g where g.user.id=:userId and g.status=GameSessionStatus.COMPLETED")
    long sumRepsByUser(@org.springframework.data.repository.query.Param("userId") String userId);

    Optional<GameSessionEntity> findByIdAndUserId(UUID id, String userId);

    Optional<GameSessionEntity> findByUserIdAndClientRequestId(String userId, UUID requestId);

    Page<GameSessionEntity> findAllByUserIdOrderByStartedAtDesc(String userId, Pageable pageable);
}
