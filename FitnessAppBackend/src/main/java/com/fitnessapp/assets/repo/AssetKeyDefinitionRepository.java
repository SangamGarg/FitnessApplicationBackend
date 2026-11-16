package com.fitnessapp.assets.repo;

import com.fitnessapp.assets.model.enums.AssetCategory;
import com.fitnessapp.assets.model.entity.AssetKeyDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetKeyDefinitionRepository extends JpaRepository<AssetKeyDefinitionEntity, Long> {
    Optional<AssetKeyDefinitionEntity> findByAssetKeyIgnoreCase(String assetKey);
    boolean existsByAssetKeyIgnoreCase(String assetKey);
    List<AssetKeyDefinitionEntity> findAllByCategoryAndEnabledTrueOrderByTitleAsc(AssetCategory category);
    List<AssetKeyDefinitionEntity> findAllByEnabledTrueOrderByCategoryAscTitleAsc();
}
