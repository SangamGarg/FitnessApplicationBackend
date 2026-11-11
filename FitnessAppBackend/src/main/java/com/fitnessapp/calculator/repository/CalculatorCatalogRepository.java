package com.fitnessapp.calculator.repository;

import com.fitnessapp.calculator.model.entities.CalculatorCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculatorCatalogRepository extends JpaRepository<CalculatorCatalogEntity, String> {
}
