package com.fitnessapp.portal.dev.assetUploadDev.service;


import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetResponseDTO;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetMetadataUpdateRequestDTO;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetUploadRequestDTO;
import org.springframework.web.multipart.MultipartFile;

public interface PortalMediaAssetService {

    AppMediaAssetResponseDTO uploadNewAsset(AssetUploadRequestDTO requestDTO, MultipartFile file);

    AppMediaAssetResponseDTO patchAssetImage(String assetKey, MultipartFile file);

    AppMediaAssetResponseDTO updateAssetMetadata(String assetKey, AssetMetadataUpdateRequestDTO requestDTO);

    AppMediaAssetResponseDTO toggleAssetStatus(String assetKey, boolean isActive);
}