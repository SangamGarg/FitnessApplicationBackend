package com.fitnessapp.support.portalSupport.repo;

import com.fitnessapp.support.portalSupport.models.entity.AppInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppInfoRepository extends JpaRepository<AppInfoEntity, Long> {
}
