package com.fitnessapp.profile.repository;

import com.fitnessapp.profile.model.entity.BadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<BadgeEntity, Long> {
    List<BadgeEntity> findByActiveTrueOrderByIdAsc();
    Optional<BadgeEntity> findByIdAndActiveTrue(Long id);
}
