package com.fitnessapp.DeveloperAndTestingService.userAndAuthDevService;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTestingRepository extends JpaRepository<UserTestingEntity, Long> {
    UserTestingEntity findByNameIgnoreCase(String name);
}