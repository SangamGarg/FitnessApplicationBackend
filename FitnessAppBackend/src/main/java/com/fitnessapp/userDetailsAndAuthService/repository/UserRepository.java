package com.fitnessapp.userDetailsAndAuthService.repository;

import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
}