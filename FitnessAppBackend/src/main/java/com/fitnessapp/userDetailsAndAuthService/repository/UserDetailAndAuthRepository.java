package com.fitnessapp.userDetailsAndAuthService.repository;

import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailAndAuthRepository extends JpaRepository<UserDetailsEntity, Long> {
    UserDetailsEntity findByUserId(String userId);
}
