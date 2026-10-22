package com.fitnessapp.portal.dev.imagesUploadDev;


import com.fitnessapp.portal.dev.imagesUploadDev.models.AppMediaAssetResponseDTO;
import com.fitnessapp.portal.dev.imagesUploadDev.models.AssetMetadataUpdateRequestDTO;
import com.fitnessapp.portal.dev.imagesUploadDev.models.AssetUploadRequestDTO;
import org.springframework.web.multipart.MultipartFile;

public interface PortalMediaAssetService {

    AppMediaAssetResponseDTO uploadNewAsset(AssetUploadRequestDTO requestDTO, MultipartFile file);

    AppMediaAssetResponseDTO patchAssetImage(String assetKey, MultipartFile file);

    AppMediaAssetResponseDTO updateAssetMetadata(String assetKey, AssetMetadataUpdateRequestDTO requestDTO);

    AppMediaAssetResponseDTO toggleAssetStatus(String assetKey, boolean isActive);
}