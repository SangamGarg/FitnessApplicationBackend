package com.fitnessapp.fitnessCalculatorsService.repositories;

import com.fitnessapp.fitnessCalculatorsService.models.entities.CalculatorImageAndAboutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalculatorImageAndAboutRepository extends JpaRepository<CalculatorImageAndAboutEntity, Long> {

    Optional<CalculatorImageAndAboutEntity> findByNameIgnoreCase(String name);

}