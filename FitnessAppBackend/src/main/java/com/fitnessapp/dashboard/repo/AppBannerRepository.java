package com.fitnessapp.dashboard.repo;

import com.fitnessapp.dashboard.dto.entity.AppBannerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AppBannerRepository extends JpaRepository<AppBannerEntity, Long> {
    boolean existsByBannerKey(String bannerKey);

    boolean existsByBannerKeyAndIdNot(String bannerKey, Long id);

    @Query("""
            SELECT b FROM AppBannerEntity b
            WHERE b.active = true
            AND (b.startAt IS NULL OR b.startAt <= :now)
            AND (b.endAt IS NULL OR b.endAt > :now)
            ORDER BY b.displayOrder ASC, b.id DESC
            """)
    List<AppBannerEntity> findCurrentlyActive(@Param("now") Instant now);
}