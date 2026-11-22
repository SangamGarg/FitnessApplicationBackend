package com.fitnessapp.assets.controller;

import com.fitnessapp.assets.model.dto.response.AppMediaAssetResponseDTO;
import com.fitnessapp.assets.model.enums.AssetCategory;
import com.fitnessapp.assets.model.ClientAssetResult;
import com.fitnessapp.assets.service.ClientMediaAssetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_ASSET_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(APP_ASSET_ENDPOINT)
@RequiredArgsConstructor
public class ClientMediaAssetController {
    private final ClientMediaAssetService clientService;

    @GetMapping("/{assetKey}")
    public ResponseEntity<AppMediaAssetResponseDTO> getAssetByKey(
            @PathVariable String assetKey, HttpServletRequest request) {
        log.info("Client request to fetch an asset by key");
        return cachedResponse(clientService.getActiveAssetByKey(assetKey), request);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<AppMediaAssetResponseDTO>> getAssetsByCategory(
            @PathVariable AssetCategory category, HttpServletRequest request) {
        log.info("Client request to fetch assets under category: [{}]", category);
        return cachedResponse(clientService.getActiveAssetsByCategory(category), request);
    }

    @GetMapping
    public ResponseEntity<List<AppMediaAssetResponseDTO>> getAllActiveAssets(HttpServletRequest request) {
        log.info("Client request to load all active assets");
        return cachedResponse(clientService.getAllActiveAssets(), request);
    }

    private <T> ResponseEntity<T> cachedResponse(ClientAssetResult<T> result, HttpServletRequest request) {
        CacheControl cacheControl = CacheControl.maxAge(300, TimeUnit.SECONDS).cachePrivate();
        if (matchesIfNoneMatch(request.getHeader(HttpHeaders.IF_NONE_MATCH), result.etag())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(result.etag())
                    .cacheControl(cacheControl)
                    .build();
        }
        return ResponseEntity.ok()
                .eTag(result.etag())
                .cacheControl(cacheControl)
                .body(result.body());
    }

    private boolean matchesIfNoneMatch(String ifNoneMatch, String currentEtag) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank()) {
            return false;
        }
        for (String candidate : ifNoneMatch.split(",")) {
            String tag = candidate.trim();
            if ("*".equals(tag) || withoutWeakPrefix(tag).equals(withoutWeakPrefix(currentEtag))) {
                return true;
            }
        }
        return false;
    }

    private String withoutWeakPrefix(String etag) {
        return etag.startsWith("W/") ? etag.substring(2) : etag;
    }
}
