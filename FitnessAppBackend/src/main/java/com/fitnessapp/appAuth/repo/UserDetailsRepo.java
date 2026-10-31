package com.fitnessapp.appAuth.repo;

import com.fitnessapp.appAuth.model.entity.UserDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepo extends JpaRepository<UserDetailsEntity, Long> {

    UserDetailsEntity findByUserId(String userId);
}
