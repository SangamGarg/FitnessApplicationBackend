package com.fitnessapp.assets.service;

import com.fitnessapp.assets.model.ClientAssetResult;
import com.fitnessapp.assets.util.AssetMapper;
import com.fitnessapp.assets.model.dto.response.AppMediaAssetResponseDTO;
import com.fitnessapp.assets.model.entity.AppMediaAssetEntity;
import com.fitnessapp.assets.model.enums.AssetCategory;
import com.fitnessapp.assets.model.enums.AssetStatus;
import com.fitnessapp.assets.repo.AppMediaAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientMediaAssetServiceImpl implements ClientMediaAssetService {
    private final AppMediaAssetRepository repository;
    private static final String KEY_REGEX = "[A-Z0-9]+(?:_[A-Z0-9]+)*";

    @Override
    public ClientAssetResult<AppMediaAssetResponseDTO> getActiveAssetByKey(String assetKey) {
        String key = normalizeAssetKey(assetKey);
        AppMediaAssetEntity entity = repository.findByAssetKeyAndStatus(key, AssetStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active asset not found"));
        return new ClientAssetResult<>(
                AssetMapper.toAssetResponse(entity),
                quote("asset:" + entity.getId() + ":" + entity.getUpdatedAt())
        );
    }

    @Override
    public ClientAssetResult<List<AppMediaAssetResponseDTO>> getActiveAssetsByCategory(AssetCategory category) {
        List<AppMediaAssetEntity> entities =
                repository.findByCategoryAndStatusOrderByTitleAsc(category, AssetStatus.ACTIVE);
        String fingerprint = entities.stream()
                .sorted(Comparator.comparing(AppMediaAssetEntity::getId))
                .map(entity -> entity.getId() + ":" + entity.getUpdatedAt())
                .collect(java.util.stream.Collectors.joining("|"));
        return new ClientAssetResult<>(
                entities.stream().map(AssetMapper::toAssetResponse).toList(),
                quote("category:" + fingerprint)
        );
    }

    @Override
    public ClientAssetResult<List<AppMediaAssetResponseDTO>> getAllActiveAssets() {
        List<AppMediaAssetEntity> entities = repository.findAllByStatusOrderByCategoryAscTitleAsc(AssetStatus.ACTIVE);
        String maxUpdatedAt = entities.stream()
                .map(AppMediaAssetEntity::getUpdatedAt)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .map(Object::toString)
                .orElse("empty");
        return new ClientAssetResult<>(
                entities.stream().map(AssetMapper::toAssetResponse).toList(),
                quote("all:" + maxUpdatedAt + ":" + entities.size())
        );
    }

    private String quote(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return "\"" + HexFormat.of().formatHex(digest) + "\"";
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
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
