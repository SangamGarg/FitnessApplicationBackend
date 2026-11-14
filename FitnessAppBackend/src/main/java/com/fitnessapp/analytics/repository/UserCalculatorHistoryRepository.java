package com.fitnessapp.analytics.repository;

import com.fitnessapp.analytics.model.entity.UserCalculatorHistoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCalculatorHistoryRepository extends JpaRepository<UserCalculatorHistoryEntity, Long> {

    Optional<UserCalculatorHistoryEntity> findByUserIdAndCalculatorKey(String userId, String calculatorKey);

    List<UserCalculatorHistoryEntity> findByUserIdOrderByLastUsedAtDesc(String userId, Pageable pageable);
}
