package com.fitnessapp.gamify.gamification.repository;

import com.fitnessapp.gamify.gamification.model.enums.GameType;
import com.fitnessapp.gamify.gamification.model.dto.response.GameUserStats;
import com.fitnessapp.gamify.gamification.model.entity.GameSessionEntity;
import com.fitnessapp.gamify.gamification.model.enums.GameSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSessionEntity, UUID> {
    long countByUserIdAndStatus(String userId, GameSessionStatus status);

    @Query("select coalesce(sum(g.repsDetected),0) from GameSessionEntity g where g.user.id=:userId and g.status=GameSessionStatus.COMPLETED")
    long sumRepsByUser(@org.springframework.data.repository.query.Param("userId") String userId);

    Optional<GameSessionEntity> findByIdAndUserId(UUID id, String userId);

    Optional<GameSessionEntity> findByUserIdAndClientRequestId(String userId, UUID requestId);

    Page<GameSessionEntity> findAllByUserIdOrderByStartedAtDesc(String userId, Pageable pageable);


    @Query("select new com.fitnessapp.gamify.gamification.model.dto.response.GameUserStats(count(g), max(g.score)) " +
            "from GameSessionEntity g where g.user.id = :userId " +
            "and g.gameType = :gameType and g.status = com.fitnessapp.gamify.gamification.model.enums.GameSessionStatus.COMPLETED")
    GameUserStats statsForUser(@Param("userId") String userId, @Param("gameType") GameType gameType);

}
