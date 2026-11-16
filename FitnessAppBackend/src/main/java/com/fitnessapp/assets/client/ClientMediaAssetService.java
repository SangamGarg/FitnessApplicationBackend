package com.fitnessapp.assets.client;
import com.fitnessapp.assets.model.enums.AssetCategory;
import com.fitnessapp.assets.model.dto.response.AppMediaAssetResponseDTO;

import java.util.List;

public interface ClientMediaAssetService {

    AppMediaAssetResponseDTO getActiveAssetByKey(String assetKey);

    List<AppMediaAssetResponseDTO> getActiveAssetsByCategory(AssetCategory category);

    List<AppMediaAssetResponseDTO> getAllActiveAssets();
}