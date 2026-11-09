package com.fitnessapp.support.portalSupport.repo;

import com.fitnessapp.support.portalSupport.models.entity.AppInfoAboutUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppInfoAboutUsRepository
        extends JpaRepository<AppInfoAboutUs, Long> {
}