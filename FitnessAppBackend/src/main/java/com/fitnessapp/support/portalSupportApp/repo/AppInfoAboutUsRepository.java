package com.fitnessapp.support.portalSupportApp.repo;

import com.fitnessapp.support.portalSupportApp.models.entity.AppInfoAboutUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppInfoAboutUsRepository
        extends JpaRepository<AppInfoAboutUs, Long> {
}