package com.fitnessapp.gamification.repository;
import com.fitnessapp.gamification.entity.UserStreakEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserStreakRepository extends JpaRepository<UserStreakEntity,Long>{
 Optional<UserStreakEntity> findByUserId(String userId);
}
