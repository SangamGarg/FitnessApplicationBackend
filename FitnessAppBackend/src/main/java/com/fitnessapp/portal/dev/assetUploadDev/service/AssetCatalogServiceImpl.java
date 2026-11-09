package com.fitnessapp.portal.dev.assetUploadDev.service;

import com.fitnessapp.portal.dev.assetUploadDev.model.*;
import com.fitnessapp.portal.dev.assetUploadDev.repo.AppMediaAssetRepository;
import com.fitnessapp.portal.dev.assetUploadDev.repo.AssetKeyDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Service implementation managing asset categories, key definitions,
 * batch registrations, and availability lifecycle states.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetCatalogServiceImpl implements AssetCatalogService {

    private static final String KEY_REGEX = "[A-Z0-9]+(?:_[A-Z0-9]+)*";

    private final AssetKeyDefinitionRepository definitions;
    private final AppMediaAssetRepository assets;

    @Override
    public List<AssetCategory> getCategories() {
        log.debug("Fetching all available asset categories");
        List<AssetCategory> categories = Arrays.asList(AssetCategory.values());
        log.trace("Retrieved {} asset categories", categories.size());
        return categories;
    }

    @Override
    public List<AssetKeyDefinitionResponseDTO> getKeys(AssetCategory category) {
        log.debug("Retrieving asset key definitions for category: [{}]", category != null ? category : "ALL");

        List<AssetKeyDefinitionEntity> rows = (category == null)
                ? definitions.findAllByEnabledTrueOrderByCategoryAscTitleAsc()
                : definitions.findAllByCategoryAndEnabledTrueOrderByTitleAsc(category);

        log.debug("Found {} active key definition(s) for category [{}]", rows.size(), category != null ? category : "ALL");

        return rows.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AssetKeyDefinitionResponseDTO registerKey(AssetKeyDefinitionRequestDTO request) {
        String normalizedKey = validateAndNormalizeKey(request.getAssetKey());
        log.info("Registering single asset key: [{}] under category: [{}]", normalizedKey, request.getCategory());

        if (definitions.existsByAssetKeyIgnoreCase(normalizedKey)) {
            log.warn("Registration rejected: asset key [{}] already exists", normalizedKey);
            throw new IllegalArgumentException("Asset key is already registered: " + normalizedKey);
        }

        AssetKeyDefinitionEntity entity = AssetKeyDefinitionEntity.builder()
                .assetKey(normalizedKey)
                .title(request.getTitle().trim())
                .category(request.getCategory())
                .altText(request.getAltText())
                .description(request.getDescription())
                .enabled(true)
                .build();

        AssetKeyDefinitionEntity saved = definitions.save(entity);
        log.info("Asset key [{}] registered successfully with ID: {}", saved.getAssetKey(), saved.getId());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public List<AssetKeyDefinitionResponseDTO> registerKeysBatch(List<AssetKeyDefinitionRequestDTO> requests) {
        log.info("Initiating batch registration for {} key definition(s)", requests.size());

        List<AssetKeyDefinitionEntity> entitiesToSave = new ArrayList<>();

        for (AssetKeyDefinitionRequestDTO req : requests) {
            String normalizedKey = validateAndNormalizeKey(req.getAssetKey());

            if (definitions.existsByAssetKeyIgnoreCase(normalizedKey)) {
                log.warn("Batch item skipped/failed: asset key [{}] already exists", normalizedKey);
                throw new IllegalArgumentException("Asset key already exists in registry: " + normalizedKey);
            }

            // Ensure no duplicate keys inside the incoming batch payload itself
            boolean duplicateInBatch = entitiesToSave.stream()
                    .anyMatch(e -> e.getAssetKey().equalsIgnoreCase(normalizedKey));
            if (duplicateInBatch) {
                log.warn("Batch rejected: duplicate key [{}] found within the payload", normalizedKey);
                throw new IllegalArgumentException("Duplicate key present in batch request: " + normalizedKey);
            }

            entitiesToSave.add(AssetKeyDefinitionEntity.builder()
                    .assetKey(normalizedKey)
                    .title(req.getTitle().trim())
                    .category(req.getCategory())
                    .altText(req.getAltText())
                    .description(req.getDescription())
                    .enabled(true)
                    .build());
        }

        List<AssetKeyDefinitionEntity> savedList = definitions.saveAll(entitiesToSave);
        log.info("Batch registration completed successfully. Saved {} record(s)", savedList.size());

        return savedList.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AssetKeyDefinitionResponseDTO toggleKeyStatus(String assetKey, boolean enabled) {
        String normalizedKey = assetKey.trim().toUpperCase(Locale.ROOT);
        log.info("Updating status of asset key [{}] to enabled: {}", normalizedKey, enabled);

        AssetKeyDefinitionEntity entity = definitions.findByAssetKeyIgnoreCase(normalizedKey)
                .orElseThrow(() -> {
                    log.warn("Key definition not found for key: [{}]", normalizedKey);
                    return new IllegalArgumentException("Asset key definition not found: " + normalizedKey);
                });

        entity.setEnabled(enabled);
        AssetKeyDefinitionEntity updated = definitions.save(entity);
        log.info("Status for key [{}] updated successfully to enabled={}", normalizedKey, enabled);

        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteKey(String assetKey) {
        String normalizedKey = assetKey.trim().toUpperCase(Locale.ROOT);
        log.info("Request received to permanently delete key definition: [{}]", normalizedKey);

        AssetKeyDefinitionEntity entity = definitions.findByAssetKeyIgnoreCase(normalizedKey)
                .orElseThrow(() -> {
                    log.warn("Delete aborted: key definition not found for key: [{}]", normalizedKey);
                    return new IllegalArgumentException("Asset key definition not found: " + normalizedKey);
                });

        // Guard clause: Prevent deleting a key if actual media files are actively mapped to it
        if (assets.existsByAssetKey(normalizedKey)) {
            log.error("Delete rejected: key [{}] has active media files associated with it", normalizedKey);
            throw new IllegalStateException("Cannot delete key definition '" + normalizedKey + "' because an uploaded media asset is using it. Disable it instead.");
        }

        definitions.delete(entity);
        log.info("Key definition [{}] permanently deleted from registry", normalizedKey);
    }

    private String validateAndNormalizeKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalArgumentException("Asset key cannot be empty or null.");
        }
        String normalized = rawKey.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches(KEY_REGEX)) {
            log.warn("Key format validation failed for input: [{}]", rawKey);
            throw new IllegalArgumentException(
                    "Asset key must use uppercase letters, numbers, and underscores (e.g., HOME_HERO_BANNER)."
            );
        }
        return normalized;
    }

    private AssetKeyDefinitionResponseDTO toResponse(AssetKeyDefinitionEntity entity) {
        boolean isUploaded = assets.existsByAssetKey(entity.getAssetKey());
        log.trace("Mapping key [{}] to DTO - uploaded: {}", entity.getAssetKey(), isUploaded);

        return AssetKeyDefinitionResponseDTO.builder()
                .assetKey(entity.getAssetKey())
                .title(entity.getTitle())
                .category(entity.getCategory())
                .altText(entity.getAltText())
                .description(entity.getDescription())
                .uploaded(isUploaded)
                .build();
    }
}