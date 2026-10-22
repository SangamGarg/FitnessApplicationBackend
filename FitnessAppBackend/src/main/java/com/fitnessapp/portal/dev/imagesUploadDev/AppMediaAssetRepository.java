package com.fitnessapp.portal.dev.imagesUploadDev;

import com.fitnessapp.portal.dev.imagesUploadDev.models.AppMediaAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppMediaAssetRepository extends JpaRepository<AppMediaAssetEntity, Long> {

    Optional<AppMediaAssetEntity> findByAssetKeyAndIsActiveTrue(String assetKey);

    Optional<AppMediaAssetEntity> findByAssetKey(String assetKey);

    boolean existsByAssetKey(String assetKey);

    List<AppMediaAssetEntity> findByCategoryAndIsActiveTrue(AssetCategory category);

    List<AppMediaAssetEntity> findAllByIsActiveTrue();
}