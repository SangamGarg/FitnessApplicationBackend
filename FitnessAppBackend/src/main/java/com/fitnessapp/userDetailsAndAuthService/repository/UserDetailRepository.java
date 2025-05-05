package com.fitnessapp.userDetailsAndAuthService.repository;

import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseLevelEntity;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetailsEntity, Long> {
    UserDetailsEntity findByUserId(String userId);
}
