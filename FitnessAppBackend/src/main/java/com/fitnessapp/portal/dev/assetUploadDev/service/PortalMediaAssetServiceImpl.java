package com.fitnessapp.portal.dev.assetUploadDev.service;

import com.fitnessapp.portal.common.CurrentUserService;
import com.fitnessapp.portal.dev.assetUploadDev.repo.AppMediaAssetRepository;
import com.fitnessapp.portal.dev.assetUploadDev.repo.AssetKeyDefinitionRepository;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetKeyDefinitionEntity;
import java.util.Locale;
import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetEntity;
import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetResponseDTO;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetMetadataUpdateRequestDTO;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetUploadRequestDTO;
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
    private final AssetKeyDefinitionRepository definitionRepository;
    private final FileStorageService storageService;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public AppMediaAssetResponseDTO uploadNewAsset(AssetUploadRequestDTO requestDTO, MultipartFile file) {
        String assetKey = requestDTO.getAssetKey().trim().toUpperCase(Locale.ROOT);
        log.info("Starting asset upload process for registered assetKey: [{}]", assetKey);

        if (repository.existsByAssetKey(assetKey)) {
            throw new IllegalArgumentException("Asset key '" + assetKey + "' already has an uploaded asset. Use the image replacement endpoint to replace it.");
        }

        AssetKeyDefinitionEntity definition = definitionRepository.findByAssetKeyIgnoreCase(assetKey)
                .filter(d -> Boolean.TRUE.equals(d.getEnabled()))
                .orElseThrow(() -> new IllegalArgumentException("Asset key is not registered or disabled: " + assetKey));

        if (requestDTO.getCategory() != null && requestDTO.getCategory() != definition.getCategory()) {
            throw new IllegalArgumentException("Selected category does not match the registered category for asset key " + assetKey);
        }

        String folder = definition.getCategory().name().toLowerCase(Locale.ROOT);
        String uploadedUrl = storageService.uploadFile(file, folder);

        AppMediaAssetEntity entity = AppMediaAssetEntity.builder()
                .assetKey(assetKey)
                .title(requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()
                        ? definition.getTitle() : requestDTO.getTitle().trim())
                .category(definition.getCategory())
                .imageUrl(uploadedUrl)
                .altText(requestDTO.getAltText() != null ? requestDTO.getAltText() : definition.getAltText())
                .description(requestDTO.getDescription() != null ? requestDTO.getDescription() : definition.getDescription())
                .isActive(true)
                .updatedByEmail(getCurrentUserEmail())
                .assetExtension(getFileExtension(file))
                .fileSizeBytes(file.getSize())
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

        String oldUrl = entity.getImageUrl();
        entity.setImageUrl(newUploadedUrl);
        entity.setAssetExtension(getFileExtension(file));
        entity.setFileSizeBytes(file.getSize());
        entity.setUpdatedByEmail(getCurrentUserEmail());
        AppMediaAssetEntity updated = repository.save(entity);
        // Delete only after the new URL has been persisted. Storage/database transactions are not atomic;
        // a production storage adapter should ideally schedule this cleanup after transaction commit.
        if (oldUrl != null && !oldUrl.equals(newUploadedUrl)) {
            storageService.deleteFileByUrl(oldUrl);
        }

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
        if (requestDTO.getCategory() != null && requestDTO.getCategory() != entity.getCategory()) {
            throw new IllegalArgumentException("Asset category is fixed by its registered asset key and cannot be changed here.");
        }

        entity.setUpdatedByEmail(getCurrentUserEmail());
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
        entity.setUpdatedByEmail(getCurrentUserEmail());
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

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }

        int lastDotIndex = originalFilename.lastIndexOf('.');

        if (lastDotIndex < 0 || lastDotIndex == originalFilename.length() - 1) {
            return null;
        }

        return originalFilename.substring(lastDotIndex + 1)
                .toLowerCase(java.util.Locale.ROOT);
    }

    private String getCurrentUserEmail() {
        return currentUserService.getEmail();
    }
}