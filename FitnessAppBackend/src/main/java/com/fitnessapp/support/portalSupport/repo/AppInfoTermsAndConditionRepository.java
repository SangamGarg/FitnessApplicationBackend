package com.fitnessapp.support.portalSupport.repo;

import com.fitnessapp.support.portalSupport.models.entity.AppInfoTermsAndCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppInfoTermsAndConditionRepository
        extends JpaRepository<AppInfoTermsAndCondition, Long> {
}