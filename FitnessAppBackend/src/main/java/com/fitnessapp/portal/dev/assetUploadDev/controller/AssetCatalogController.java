package com.fitnessapp.portal.dev.assetUploadDev.controller;

import com.fitnessapp.portal.dev.assetUploadDev.model.AssetCategory;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetKeyDefinitionRequestDTO;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetKeyDefinitionResponseDTO;
import com.fitnessapp.portal.dev.assetUploadDev.service.AssetCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_ASSET_ENDPOINT;

/**
 * Controller for managing the catalog of asset categories and registered key slots.
 */
@Slf4j
@RestController
@RequestMapping(PORTAL_ASSET_ENDPOINT + "/catalog")
@RequiredArgsConstructor
@Tag(name = "Asset Catalog", description = "Endpoints for managing asset categories and key definition slots")
public class AssetCatalogController {

    private final AssetCatalogService service;


    @GetMapping("/categories")
    public ResponseEntity<List<AssetCategory>> getCategories() {
        log.info("REST request to fetch all asset categories for catalog dropdown");
        List<AssetCategory> categories = service.getCategories();
        return ResponseEntity.ok(categories);
    }


    @GetMapping("/keys")
    public ResponseEntity<List<AssetKeyDefinitionResponseDTO>> getKeys(
            @Parameter(description = "Optional category filter to restrict key definitions", example = "APP_BRANDING")
            @RequestParam(required = false) AssetCategory category) {
        log.info("REST request to fetch catalog keys filtered by category: [{}]", category != null ? category : "ALL");
        List<AssetKeyDefinitionResponseDTO> keys = service.getKeys(category);
        return ResponseEntity.ok(keys);
    }


    @PostMapping("/keys")
    public ResponseEntity<AssetKeyDefinitionResponseDTO> registerKey(
            @Parameter(description = "Asset key definition details", required = true)
            @Valid @RequestBody AssetKeyDefinitionRequestDTO request) {
        log.info("REST request to register new catalog key: [{}] under category: [{}]",
                request.getAssetKey(), request.getCategory());
        AssetKeyDefinitionResponseDTO response = service.registerKey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/keys/batch")
    public ResponseEntity<List<AssetKeyDefinitionResponseDTO>> registerKeysBatch(
            @Parameter(description = "List of asset key definitions to register", required = true)
            @Valid @RequestBody List<AssetKeyDefinitionRequestDTO> requests) {
        log.info("REST request to batch-register {} catalog key definitions", requests.size());
        List<AssetKeyDefinitionResponseDTO> responses = service.registerKeysBatch(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PatchMapping("/keys/{assetKey}/status")
    public ResponseEntity<AssetKeyDefinitionResponseDTO> toggleKeyStatus(
            @Parameter(description = "The unique asset key identifier", required = true, example = "HOME_HERO_BANNER")
            @PathVariable String assetKey,
            @Parameter(description = "Target enabled state", required = true, example = "false")
            @RequestParam boolean enabled) {
        log.info("REST request to toggle status of catalog key: [{}] to enabled={}", assetKey, enabled);
        AssetKeyDefinitionResponseDTO response = service.toggleKeyStatus(assetKey, enabled);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/keys/{assetKey}")
    public ResponseEntity<Void> deleteKey(
            @Parameter(description = "The unique asset key identifier to permanently remove", required = true, example = "OLD_PROMO_BANNER")
            @PathVariable String assetKey) {
        log.info("REST request to delete catalog key definition: [{}]", assetKey);
        service.deleteKey(assetKey);
        return ResponseEntity.noContent().build();
    }
}