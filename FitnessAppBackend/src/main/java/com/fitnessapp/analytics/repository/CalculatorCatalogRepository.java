package com.fitnessapp.analytics.repository;

import com.fitnessapp.analytics.model.entity.CalculatorCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculatorCatalogRepository extends JpaRepository<CalculatorCatalogEntity, String> {
    List<CalculatorCatalogEntity> findByPopularTrueOrderByDisplayOrderAsc();
}
