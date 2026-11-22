package com.fitnessapp.assets.service;

import com.fitnessapp.assets.model.dto.request.AssetKeyDefinitionRequestDTO;
import com.fitnessapp.assets.model.dto.request.AssetMetadataUpdateRequestDTO;
import com.fitnessapp.assets.model.dto.request.AssetUploadRequestDTO;
import com.fitnessapp.assets.model.dto.response.AppMediaAssetResponseDTO;
import com.fitnessapp.assets.model.dto.response.AssetKeyDefinitionResponseDTO;
import com.fitnessapp.assets.model.entity.AppMediaAssetEntity;
import com.fitnessapp.assets.model.enums.AssetCategory;
import com.fitnessapp.assets.model.enums.AssetStatus;
import com.fitnessapp.assets.repo.AppMediaAssetRepository;
import com.fitnessapp.assets.util.AssetMapper;
import com.fitnessapp.assets.util.ImageFileValidator;
import com.fitnessapp.assets.util.ImageFileValidator.ValidatedImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalMediaAssetServiceImpl implements PortalMediaAssetService {
    private final AppMediaAssetRepository repository;
    private final FileStorageService storageService;
    private final ImageFileValidator imageFileValidator;
    private final PlatformTransactionManager transactionManager;
    private static final int MAX_BATCH_SIZE = 100;
    private static final String KEY_REGEX = "[A-Z0-9]+(?:_[A-Z0-9]+)*";


    @Override
    public AppMediaAssetResponseDTO uploadNewAsset(AssetUploadRequestDTO requestDTO, MultipartFile file) {
        String key = normalizeAssetKey(requestDTO == null ? null : requestDTO.getAssetKey());
        AppMediaAssetEntity initial = findAsset(key);
        ensureUploadable(initial);
        ensureCategoryMatches(requestDTO.getCategory(), initial);

        ValidatedImage image = imageFileValidator.validate(file);
        String uploadedUrl = storageService.uploadFile(file, initial.getCategory().name().toLowerCase(java.util.Locale.ROOT));
        String oldUrl;
        AppMediaAssetEntity persisted;
        try {
            PersistedAsset result = transactionTemplate().execute(status -> {
                AppMediaAssetEntity current = findAsset(key);
                ensureUploadable(current);
                ensureCategoryMatches(requestDTO.getCategory(), current);
                applyOptionalMetadata(current, requestDTO);
                current.setImageUrl(uploadedUrl);
                current.setAssetExtension(image.extension());
                current.setFileSizeBytes(image.sizeBytes());
                current.setStatus(AssetStatus.ACTIVE);
                current.setUpdatedByEmail(getCurrentUserPortal());
                return new PersistedAsset(repository.saveAndFlush(current), null);
            });
            persisted = result.entity();
            oldUrl = result.oldUrl();
        } catch (RuntimeException exception) {
            deleteBestEffort(uploadedUrl);
            throw exception;
        }
        deleteBestEffort(oldUrl);
        return AssetMapper.toAssetResponse(persisted);
    }

    @Override
    public AppMediaAssetResponseDTO patchAssetImage(String assetKey, MultipartFile file) {
        String key = normalizeAssetKey(assetKey);
        AppMediaAssetEntity initial = findAsset(key);
        ensureImageExists(initial);

        ValidatedImage image = imageFileValidator.validate(file);
        String uploadedUrl = storageService.uploadFile(file, initial.getCategory().name().toLowerCase(java.util.Locale.ROOT));
        AppMediaAssetEntity persisted;
        String oldUrl;
        try {
            PersistedAsset result = transactionTemplate().execute(status -> {
                AppMediaAssetEntity current = findAsset(key);
                ensureImageExists(current);
                String previousUrl = current.getImageUrl();
                current.setImageUrl(uploadedUrl);
                current.setAssetExtension(image.extension());
                current.setFileSizeBytes(image.sizeBytes());
                current.setUpdatedByEmail(getCurrentUserPortal());
                return new PersistedAsset(repository.saveAndFlush(current), previousUrl);
            });
            persisted = result.entity();
            oldUrl = result.oldUrl();
        } catch (RuntimeException exception) {
            deleteBestEffort(uploadedUrl);
            throw exception;
        }
        if (!uploadedUrl.equals(oldUrl)) {
            deleteBestEffort(oldUrl);
        }
        return AssetMapper.toAssetResponse(persisted);
    }

    @Override
    @Transactional
    public AppMediaAssetResponseDTO updateAssetMetadata(String assetKey, AssetMetadataUpdateRequestDTO requestDTO) {
        String key = normalizeAssetKey(assetKey);
        AppMediaAssetEntity entity = findAsset(key);
        if (requestDTO.getCategory() != null && requestDTO.getCategory() != entity.getCategory()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Asset category is fixed by its registered asset key and cannot be changed here"
            );
        }

        if (requestDTO.getTitle() != null && !requestDTO.getTitle().isBlank()) {
            entity.setTitle(requestDTO.getTitle().trim());
        }
        if (requestDTO.getAltText() != null) {
            entity.setAltText(requestDTO.getAltText());
        }
        if (requestDTO.getDescription() != null) {
            entity.setDescription(requestDTO.getDescription());
        }
        entity.setUpdatedByEmail(getCurrentUserPortal());
        return AssetMapper.toAssetResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public AppMediaAssetResponseDTO toggleAssetStatus(String assetKey, boolean isActive) {
        String key = normalizeAssetKey(assetKey);
        AppMediaAssetEntity entity = findAsset(key);
        if (isActive && entity.getImageUrl() == null) {
            throw conflict("An asset must have an uploaded image before it can be enabled");
        }

        entity.setStatus(isActive ? AssetStatus.ACTIVE : AssetStatus.DISABLED);
        entity.setUpdatedByEmail(getCurrentUserPortal());
        return AssetMapper.toAssetResponse(repository.save(entity));
    }

    @Override
    public AppMediaAssetResponseDTO deleteAssetImage(String assetKey) {
        String key = normalizeAssetKey(assetKey);
        PersistedAsset result = transactionTemplate().execute(status -> {
            AppMediaAssetEntity entity = findAsset(key);
            ensureImageExists(entity);
            String oldUrl = entity.getImageUrl();
            entity.setImageUrl(null);
            entity.setFileSizeBytes(null);
            entity.setAssetExtension(null);
            entity.setStatus(AssetStatus.PENDING_UPLOAD);
            entity.setUpdatedByEmail(getCurrentUserPortal());
            return new PersistedAsset(repository.saveAndFlush(entity), oldUrl);
        });
        deleteBestEffort(result.oldUrl());
        return AssetMapper.toAssetResponse(result.entity());
    }



    /*
     * Catalog
     * */


    @Transactional(readOnly = true)
    @Override
    public List<AssetCategory> getCategories() {
        return Arrays.asList(AssetCategory.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetKeyDefinitionResponseDTO> getKeys(AssetCategory category) {
        List<AppMediaAssetEntity> rows = category == null
                ? repository.findAllByStatusNotOrderByCategoryAscTitleAsc(AssetStatus.DISABLED)
                : repository.findByCategoryAndStatusNotOrderByTitleAsc(category, AssetStatus.DISABLED);
        return rows.stream().map(AssetMapper::toKeyResponse).toList();
    }

    @Override
    @Transactional
    public AssetKeyDefinitionResponseDTO registerKey(AssetKeyDefinitionRequestDTO request) {
        if (request == null) {
            log.warn("registerKey called with null request");
            throw badRequest("Asset key definition is required");
        }

        String rawKey = request.getAssetKey();
        String key = normalizeAssetKey(rawKey);
        log.info("Attempting to register asset key. rawKey='{}', normalizedKey='{}'", rawKey, key);

        var existingAsset = repository.findByAssetKey(key);
        if (existingAsset.isPresent()) {
            log.warn("Conflict detected at pre-check: asset key '{}' already exists in DB with ID={}",
                    key, existingAsset.get().getId()); // Adjust getter if ID field is named differently
            throw conflict("Asset key is already registered: " + key);
        }

        AppMediaAssetEntity entity = newDefinition(request, key);
        try {
            AppMediaAssetEntity savedEntity = repository.saveAndFlush(entity);
            log.info("Successfully registered asset key '{}' with ID={}", key, savedEntity.getId());
            return AssetMapper.toKeyResponse(savedEntity);
        } catch (DataIntegrityViolationException exception) {
            exception.getMostSpecificCause();
            String rootCauseMsg = exception.getMostSpecificCause().getMessage();
            log.error("DataIntegrityViolationException while saving asset key '{}'. Root cause: {}",
                    key, rootCauseMsg, exception);
            throw conflict("Asset key is already registered: " + key);
        }
    }

    @Override
    @Transactional
    public List<AssetKeyDefinitionResponseDTO> registerKeysBatch(List<AssetKeyDefinitionRequestDTO> requests) {
        if (requests == null || requests.isEmpty() || requests.size() > MAX_BATCH_SIZE) {
            throw badRequest("Batch must contain between 1 and 100 asset keys");
        }

        List<AppMediaAssetEntity> entities = new ArrayList<>(requests.size());
        Set<String> normalizedKeys = new HashSet<>();
        for (AssetKeyDefinitionRequestDTO request : requests) {
            if (request == null) {
                throw badRequest("Batch asset key definitions cannot contain null items");
            }
            String key = normalizeAssetKey(request.getAssetKey());
            if (!normalizedKeys.add(key)) {
                throw conflict("Duplicate key present in batch request: " + key);
            }
            entities.add(newDefinition(request, key));
        }

        Set<String> existingKeys = repository.findAllByAssetKeyIn(normalizedKeys).stream()
                .map(AppMediaAssetEntity::getAssetKey)
                .collect(java.util.stream.Collectors.toSet());
        if (!existingKeys.isEmpty()) {
            throw conflict("Asset key is already registered: " + existingKeys.iterator().next());
        }

        try {
            return repository.saveAllAndFlush(entities).stream()
                    .map(AssetMapper::toKeyResponse)
                    .toList();
        } catch (DataIntegrityViolationException exception) {
            throw conflict("One or more asset keys are already registered");
        }
    }

    @Override
    @Transactional
    public AssetKeyDefinitionResponseDTO toggleKeyStatus(String assetKey, boolean enabled) {
        String key = normalizeAssetKey(assetKey);
        AppMediaAssetEntity entity = repository.findByAssetKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset key not found"));

        entity.setStatus(!enabled
                ? AssetStatus.DISABLED
                : entity.getImageUrl() == null ? AssetStatus.PENDING_UPLOAD : AssetStatus.ACTIVE);
        return AssetMapper.toKeyResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteKey(String assetKey) {
        String key = normalizeAssetKey(assetKey);
        AppMediaAssetEntity entity = repository.findByAssetKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset key not found"));
        if (entity.getImageUrl() != null) {
            throw conflict("Remove the uploaded image first or disable the key");
        }
        repository.delete(entity);
    }

    private AppMediaAssetEntity newDefinition(AssetKeyDefinitionRequestDTO request, String key) {
        return AppMediaAssetEntity.builder()
                .assetKey(key)
                .title(request.getTitle().trim())
                .category(request.getCategory())
                .altText(request.getAltText())
                .description(request.getDescription())
                .status(AssetStatus.PENDING_UPLOAD)
                .build();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }

    private AppMediaAssetEntity findAsset(String key) {
        return repository.findByAssetKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset key not found"));
    }

    private void ensureUploadable(AppMediaAssetEntity entity) {
        if (entity.getStatus() == AssetStatus.DISABLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset key is disabled");
        }
        if (entity.getImageUrl() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset already has an uploaded image; use the image replacement endpoint"
            );
        }
    }

    private void ensureImageExists(AppMediaAssetEntity entity) {
        if (entity.getImageUrl() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset has no image; use the upload endpoint");
        }
    }

    private void ensureCategoryMatches(AssetCategory requestedCategory,
                                       AppMediaAssetEntity entity) {
        if (requestedCategory != null && requestedCategory != entity.getCategory()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected category does not match the registered category for this asset key"
            );
        }
    }

    private void applyOptionalMetadata(AppMediaAssetEntity entity, AssetUploadRequestDTO request) {
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            entity.setTitle(request.getTitle().trim());
        }
        if (request.getAltText() != null) {
            entity.setAltText(request.getAltText());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
    }

    private void deleteBestEffort(String url) {
        if (url == null) {
            return;
        }
        try {
            storageService.deleteFileByUrl(url);
        } catch (RuntimeException exception) {
            log.warn("Storage cleanup failed after an asset update");
        }
    }

    private String getCurrentUserPortal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private record PersistedAsset(AppMediaAssetEntity entity, String oldUrl) {
    }

    private String normalizeAssetKey(String raw) {
        if (raw == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset key is required");
        }

        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty() || normalized.length() > 100 || !normalized.matches(KEY_REGEX)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Asset key must be 1-100 characters using letters, numbers, and single underscores"
            );
        }
        return normalized;
    }
}
