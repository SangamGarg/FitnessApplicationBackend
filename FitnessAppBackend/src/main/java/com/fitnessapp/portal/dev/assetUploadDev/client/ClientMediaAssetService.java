package com.fitnessapp.portal.dev.assetUploadDev.client;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetCategory;
import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetResponseDTO;

import java.util.List;

public interface ClientMediaAssetService {

    AppMediaAssetResponseDTO getActiveAssetByKey(String assetKey);

    List<AppMediaAssetResponseDTO> getActiveAssetsByCategory(AssetCategory category);

    List<AppMediaAssetResponseDTO> getAllActiveAssets();
}