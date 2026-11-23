package com.fitnessapp.profile.repository;

import com.fitnessapp.profile.model.entity.UserBadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadgeEntity, Long> {
    boolean existsByUserIdAndBadgeIdAndUnlockedTrue(String userId, Long badgeId);

    boolean existsByBadgeId(Long badgeId);

    Optional<UserBadgeEntity> findByUserIdAndBadgeId(String userId, Long badgeId);

    List<UserBadgeEntity> findByUserIdAndUnlockedTrueOrderByUnlockedAtDesc(String userId);
}
