package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.repository;

import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.entity.AppInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppInfoRepository extends JpaRepository<AppInfoEntity, Long> {
}
