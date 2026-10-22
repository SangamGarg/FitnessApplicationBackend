package com.fitnessapp.portal.dev.imagesUploadDev;

import com.fitnessapp.portal.dev.imagesUploadDev.AppMediaAssetRepository;
import com.fitnessapp.portal.dev.imagesUploadDev.models.AppMediaAssetEntity;
import com.fitnessapp.portal.dev.imagesUploadDev.models.AppMediaAssetResponseDTO;
import com.fitnessapp.portal.dev.imagesUploadDev.models.AssetMetadataUpdateRequestDTO;
import com.fitnessapp.portal.dev.imagesUploadDev.models.AssetUploadRequestDTO;
import com.fitnessapp.portal.dev.imagesUploadDev.FileStorageService;
import com.fitnessapp.portal.dev.imagesUploadDev.PortalMediaAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalMediaAssetServiceImpl implements PortalMediaAssetService {

    private final AppMediaAssetRepository repository;
    private final FileStorageService storageService;

    @Override
    @Transactional
    public AppMediaAssetResponseDTO uploadNewAsset(AssetUploadRequestDTO requestDTO, MultipartFile file) {
        log.info("Starting asset upload process for assetKey: [{}]", requestDTO.getAssetKey());

        if (repository.existsByAssetKey(requestDTO.getAssetKey())) {
            log.error("Asset upload failed: assetKey [{}] already exists", requestDTO.getAssetKey());
            throw new IllegalArgumentException("Asset key '" + requestDTO.getAssetKey() + "' already exists.");
        }

        String folder = requestDTO.getCategory().name().toLowerCase();
        String uploadedUrl = storageService.uploadFile(file, folder);
        log.info("Image file uploaded successfully for key [{}]. Storage URL: {}", requestDTO.getAssetKey(), uploadedUrl);

        AppMediaAssetEntity entity = AppMediaAssetEntity.builder()
                .assetKey(requestDTO.getAssetKey())
                .title(requestDTO.getTitle())
                .category(requestDTO.getCategory())
                .imageUrl(uploadedUrl)
                .altText(requestDTO.getAltText())
                .description(requestDTO.getDescription())
                .isActive(true)
                .build();

        AppMediaAssetEntity saved = repository.save(entity);
        log.info("Asset entity persisted successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AppMediaAssetResponseDTO patchAssetImage(String assetKey, MultipartFile file) {
        log.info("Initiating image replacement patch for assetKey: [{}]", assetKey);

        AppMediaAssetEntity entity = repository.findByAssetKey(assetKey)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found for key: " + assetKey));

        String folder = entity.getCategory().name().toLowerCase();
        String newUploadedUrl = storageService.uploadFile(file, folder);
        log.info("New image binary uploaded for key [{}]. Old URL: {}, New URL: {}", assetKey, entity.getImageUrl(), newUploadedUrl);

        // Optional: Remove old file from cloud storage if needed
        storageService.deleteFileByUrl(entity.getImageUrl());

        entity.setImageUrl(newUploadedUrl);
        AppMediaAssetEntity updated = repository.save(entity);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public AppMediaAssetResponseDTO updateAssetMetadata(String assetKey, AssetMetadataUpdateRequestDTO requestDTO) {
        log.info("Updating metadata fields for assetKey: [{}]", assetKey);

        AppMediaAssetEntity entity = repository.findByAssetKey(assetKey)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found for key: " + assetKey));

        if (requestDTO.getTitle() != null && !requestDTO.getTitle().isBlank()) {
            entity.setTitle(requestDTO.getTitle());
        }
        if (requestDTO.getAltText() != null) {
            entity.setAltText(requestDTO.getAltText());
        }
        if (requestDTO.getDescription() != null) {
            entity.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getCategory() != null) {
            entity.setCategory(requestDTO.getCategory());
        }

        AppMediaAssetEntity updated = repository.save(entity);
        log.info("Asset metadata updated successfully for ID: [{}]", updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public AppMediaAssetResponseDTO toggleAssetStatus(String assetKey, boolean isActive) {
        log.info("Setting active status of asset [{}] to: {}", assetKey, isActive);

        AppMediaAssetEntity entity = repository.findByAssetKey(assetKey)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found for key: " + assetKey));

        entity.setIsActive(isActive);
        AppMediaAssetEntity saved = repository.save(entity);
        log.info("Asset [{}] status successfully updated to active={}", assetKey, isActive);

        return mapToResponse(saved);
    }

    private AppMediaAssetResponseDTO mapToResponse(AppMediaAssetEntity entity) {
        return AppMediaAssetResponseDTO.builder()
                .assetKey(entity.getAssetKey())
                .title(entity.getTitle())
                .imageUrl(entity.getImageUrl())
                .altText(entity.getAltText())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .build();
    }
}