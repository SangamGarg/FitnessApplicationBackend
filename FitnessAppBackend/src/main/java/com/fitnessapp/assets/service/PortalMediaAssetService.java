package com.fitnessapp.assets.service;


import com.fitnessapp.assets.model.dto.response.AppMediaAssetResponseDTO;
import com.fitnessapp.assets.model.dto.request.AssetMetadataUpdateRequestDTO;
import com.fitnessapp.assets.model.dto.request.AssetUploadRequestDTO;
import org.springframework.web.multipart.MultipartFile;

public interface PortalMediaAssetService {

    AppMediaAssetResponseDTO uploadNewAsset(AssetUploadRequestDTO requestDTO, MultipartFile file);

    AppMediaAssetResponseDTO patchAssetImage(String assetKey, MultipartFile file);

    AppMediaAssetResponseDTO updateAssetMetadata(String assetKey, AssetMetadataUpdateRequestDTO requestDTO);

    AppMediaAssetResponseDTO toggleAssetStatus(String assetKey, boolean isActive);
}