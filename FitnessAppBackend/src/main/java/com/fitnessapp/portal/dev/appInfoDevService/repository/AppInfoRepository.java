package com.fitnessapp.portal.dev.appInfoDevService.repository;

import com.fitnessapp.portal.dev.appInfoDevService.models.entity.AppInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppInfoRepository extends JpaRepository<AppInfoEntity, Long> {
}
