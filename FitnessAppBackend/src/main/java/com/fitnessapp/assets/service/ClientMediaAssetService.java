package com.fitnessapp.assets.service;

import com.fitnessapp.assets.model.ClientAssetResult;
import com.fitnessapp.assets.model.dto.response.AppMediaAssetResponseDTO;
import com.fitnessapp.assets.model.enums.AssetCategory;

import java.util.List;

public interface ClientMediaAssetService {
    ClientAssetResult<AppMediaAssetResponseDTO> getActiveAssetByKey(String assetKey);

    ClientAssetResult<List<AppMediaAssetResponseDTO>> getActiveAssetsByCategory(AssetCategory category);

    ClientAssetResult<List<AppMediaAssetResponseDTO>> getAllActiveAssets();
}
