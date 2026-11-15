package com.fitnessapp.gamify.achievement.repository;
import com.fitnessapp.gamify.achievement.model.AchievementCode;
import com.fitnessapp.gamify.achievement.model.UserAchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserAchievementRepository extends JpaRepository<UserAchievementEntity,Long>{ boolean existsByUserIdAndCode(String userId, AchievementCode code); List<UserAchievementEntity> findAllByUserIdOrderByUnlockedAtDesc(String userId); }
