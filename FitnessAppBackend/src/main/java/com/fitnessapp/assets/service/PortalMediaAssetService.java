package com.fitnessapp.assets.service;


import com.fitnessapp.assets.model.dto.request.AssetKeyDefinitionRequestDTO;
import com.fitnessapp.assets.model.dto.response.AppMediaAssetResponseDTO;
import com.fitnessapp.assets.model.dto.request.AssetMetadataUpdateRequestDTO;
import com.fitnessapp.assets.model.dto.request.AssetUploadRequestDTO;
import com.fitnessapp.assets.model.dto.response.AssetKeyDefinitionResponseDTO;
import com.fitnessapp.assets.model.enums.AssetCategory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PortalMediaAssetService {

    AppMediaAssetResponseDTO uploadNewAsset(AssetUploadRequestDTO requestDTO, MultipartFile file);

    AppMediaAssetResponseDTO patchAssetImage(String assetKey, MultipartFile file);

    AppMediaAssetResponseDTO updateAssetMetadata(String assetKey, AssetMetadataUpdateRequestDTO requestDTO);

    AppMediaAssetResponseDTO toggleAssetStatus(String assetKey, boolean isActive);

    AppMediaAssetResponseDTO deleteAssetImage(String assetKey);



    List<AssetCategory> getCategories();
    List<AssetKeyDefinitionResponseDTO> getKeys(AssetCategory category);
    AssetKeyDefinitionResponseDTO registerKey(AssetKeyDefinitionRequestDTO request);
    List<AssetKeyDefinitionResponseDTO> registerKeysBatch(List<AssetKeyDefinitionRequestDTO> requests);

    AssetKeyDefinitionResponseDTO toggleKeyStatus(String assetKey, boolean enabled);

    void deleteKey(String assetKey);
}