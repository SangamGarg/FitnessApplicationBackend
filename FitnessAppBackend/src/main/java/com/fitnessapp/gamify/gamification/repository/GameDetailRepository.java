package com.fitnessapp.gamify.gamification.repository;


import com.fitnessapp.gamify.gamification.model.entity.GameDetailEntity;
import com.fitnessapp.gamify.gamification.model.enums.GameType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameDetailRepository extends JpaRepository<GameDetailEntity, Long> {
    // Only the catalogue is fetched eagerly; rules/tips load lazily inside the service transaction
    // (avoids a rules x tips cartesian join).
    @EntityGraph(attributePaths = "catalogue")
    Optional<GameDetailEntity> findByCatalogueGameTypeAndCatalogueActiveTrue(GameType gameType);

    @Query("select d.catalogue.id from GameDetailEntity d")
    List<Long> findAllCatalogueIds();
}