package com.fitnessapp.calculator.repo;

import com.fitnessapp.calculator.model.entities.BurnedCaloriesActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BurnedCaloriesFromActivityRepository extends JpaRepository<BurnedCaloriesActivityEntity, Long> {
    Optional<BurnedCaloriesActivityEntity> findByNameIgnoreCase(String name);

    List<BurnedCaloriesActivityEntity> findByNameStartingWithIgnoreCase(String name);

}