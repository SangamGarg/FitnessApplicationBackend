package com.fitnessapp.portal.dev.imagesUploadDev;
import com.fitnessapp.portal.dev.imagesUploadDev.AssetCategory;
import com.fitnessapp.portal.dev.imagesUploadDev.models.AppMediaAssetResponseDTO;

import java.util.List;

public interface ClientMediaAssetService {

    AppMediaAssetResponseDTO getActiveAssetByKey(String assetKey);

    List<AppMediaAssetResponseDTO> getActiveAssetsByCategory(AssetCategory category);

    List<AppMediaAssetResponseDTO> getAllActiveAssets();
}