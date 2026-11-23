package com.fitnessapp.gamify.gamification.repository;

import com.fitnessapp.gamify.gamification.model.enums.GameType;
import com.fitnessapp.gamify.gamification.model.entity.GameCatalogueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameCatalogueRepository extends JpaRepository<GameCatalogueEntity, Long> {
    List<GameCatalogueEntity> findAllByOrderByDisplayOrderAsc();

    List<GameCatalogueEntity> findAllByActiveTrueOrderByDisplayOrderAsc();

    Optional<GameCatalogueEntity> findByGameTypeAndActiveTrue(GameType gameType);

    Optional<GameCatalogueEntity> findByGameType(GameType gameType);
}
