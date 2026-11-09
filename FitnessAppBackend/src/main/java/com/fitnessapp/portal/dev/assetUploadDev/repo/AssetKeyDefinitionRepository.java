package com.fitnessapp.portal.dev.assetUploadDev.repo;

import com.fitnessapp.portal.dev.assetUploadDev.model.AssetCategory;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetKeyDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetKeyDefinitionRepository extends JpaRepository<AssetKeyDefinitionEntity, Long> {
    Optional<AssetKeyDefinitionEntity> findByAssetKeyIgnoreCase(String assetKey);
    boolean existsByAssetKeyIgnoreCase(String assetKey);
    List<AssetKeyDefinitionEntity> findAllByCategoryAndEnabledTrueOrderByTitleAsc(AssetCategory category);
    List<AssetKeyDefinitionEntity> findAllByEnabledTrueOrderByCategoryAscTitleAsc();
}
