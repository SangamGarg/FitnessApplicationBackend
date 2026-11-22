package com.fitnessapp.assets.repo;

import com.fitnessapp.assets.model.enums.AssetCategory;
import com.fitnessapp.assets.model.entity.AppMediaAssetEntity;
import com.fitnessapp.assets.model.enums.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppMediaAssetRepository extends JpaRepository<AppMediaAssetEntity, Long> {

    Optional<AppMediaAssetEntity> findByAssetKey(String assetKey);

    Optional<AppMediaAssetEntity> findByAssetKeyAndStatus(String assetKey, AssetStatus status);

    List<AppMediaAssetEntity> findAllByAssetKeyIn(Collection<String> assetKeys);

    List<AppMediaAssetEntity> findByCategoryAndStatusOrderByTitleAsc(AssetCategory category, AssetStatus status);

    List<AppMediaAssetEntity> findAllByStatusOrderByCategoryAscTitleAsc(AssetStatus status);

    List<AppMediaAssetEntity> findAllByStatusNotOrderByCategoryAscTitleAsc(AssetStatus status);

    List<AppMediaAssetEntity> findByCategoryAndStatusNotOrderByTitleAsc(AssetCategory category, AssetStatus status);
}