package com.fitnessapp.gamification.achievement;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserAchievementRepository extends JpaRepository<UserAchievementEntity,Long>{ boolean existsByUserIdAndCode(String userId,AchievementCode code); List<UserAchievementEntity> findAllByUserIdOrderByUnlockedAtDesc(String userId); }
