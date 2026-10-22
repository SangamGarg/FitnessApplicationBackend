package com.fitnessapp.portal.dev.assetUploadDev.service;

import com.fitnessapp.portal.dev.assetUploadDev.model.*;
import java.util.List;

public interface AssetCatalogService {
    List<AssetCategory> getCategories();
    List<AssetKeyDefinitionResponseDTO> getKeys(AssetCategory category);
    AssetKeyDefinitionResponseDTO registerKey(AssetKeyDefinitionRequestDTO request);
    List<AssetKeyDefinitionResponseDTO> registerKeysBatch(List<AssetKeyDefinitionRequestDTO> requests);

    AssetKeyDefinitionResponseDTO toggleKeyStatus(String assetKey, boolean enabled);

    void deleteKey(String assetKey);
}
