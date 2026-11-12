package com.fitnessapp.analytics.repository;


import com.fitnessapp.analytics.model.entity.BodyMeasurementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurementEntity, UUID> {

    Optional<BodyMeasurementEntity> findFirstByUserIdOrderByMeasuredAtDesc(String userId);

    List<BodyMeasurementEntity> findByUserIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(String userId, LocalDateTime start, LocalDateTime end
    );
}
