package com.fitnessapp.nutrition.repository;

import com.fitnessapp.nutrition.model.entity.MealEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MealEntryRepository extends JpaRepository<MealEntryEntity, UUID> {
    List<MealEntryEntity> findAllByUser_IdAndMealDateOrderByCreatedAtAsc(String userId, LocalDate mealDate);

    Optional<MealEntryEntity> findByIdAndUser_Id(UUID id, String userId);

    Optional<MealEntryEntity> findByUser_IdAndClientMealId(String userId, UUID clientMealId);

    Page<MealEntryEntity> findAllByUser_IdOrderByMealDateDescCreatedAtDesc(String userId, Pageable pageable);

    List<MealEntryEntity> findAllByUser_IdAndMealDateBetween(String userId, LocalDate startDate, LocalDate endDate);
}
