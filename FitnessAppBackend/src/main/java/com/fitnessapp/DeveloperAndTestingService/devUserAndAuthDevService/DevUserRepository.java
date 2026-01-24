package com.fitnessapp.DeveloperAndTestingService.devUserAndAuthDevService;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DevUserRepository extends JpaRepository<DevUserEntity, Long> {
    DevUserEntity findByDevNameIgnoreCase(String name);
}