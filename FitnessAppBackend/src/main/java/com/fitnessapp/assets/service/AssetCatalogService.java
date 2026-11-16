package com.fitnessapp.assets.service;

import com.fitnessapp.assets.model.dto.request.AssetKeyDefinitionRequestDTO;
import com.fitnessapp.assets.model.dto.response.AssetKeyDefinitionResponseDTO;
import com.fitnessapp.assets.model.enums.AssetCategory;

import java.util.List;

public interface AssetCatalogService {
    List<AssetCategory> getCategories();
    List<AssetKeyDefinitionResponseDTO> getKeys(AssetCategory category);
    AssetKeyDefinitionResponseDTO registerKey(AssetKeyDefinitionRequestDTO request);
    List<AssetKeyDefinitionResponseDTO> registerKeysBatch(List<AssetKeyDefinitionRequestDTO> requests);

    AssetKeyDefinitionResponseDTO toggleKeyStatus(String assetKey, boolean enabled);

    void deleteKey(String assetKey);
}
