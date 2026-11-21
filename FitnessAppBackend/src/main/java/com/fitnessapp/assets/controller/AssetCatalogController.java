package com.fitnessapp.assets.controller;

import com.fitnessapp.assets.model.enums.AssetCategory;
import com.fitnessapp.assets.model.dto.request.AssetKeyDefinitionRequestDTO;
import com.fitnessapp.assets.model.dto.response.AssetKeyDefinitionResponseDTO;
import com.fitnessapp.assets.service.AssetCatalogService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_ASSET_ENDPOINT;


@Slf4j
@RestController
@RequestMapping(PORTAL_DEV_ASSET_ENDPOINT + "/catalog")
@RequiredArgsConstructor
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
            @RequestParam(required = false) AssetCategory category) {
        log.info("REST request to fetch catalog keys filtered by category: [{}]", category != null ? category : "ALL");
        List<AssetKeyDefinitionResponseDTO> keys = service.getKeys(category);
        return ResponseEntity.ok(keys);
    }


    @PostMapping("/keys")
    public ResponseEntity<AssetKeyDefinitionResponseDTO> registerKey(
            @Valid @RequestBody AssetKeyDefinitionRequestDTO request) {
        log.info("REST request to register new catalog key: [{}] under category: [{}]",
                request.getAssetKey(), request.getCategory());
        AssetKeyDefinitionResponseDTO response = service.registerKey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/keys/batch")
    public ResponseEntity<List<AssetKeyDefinitionResponseDTO>> registerKeysBatch(
            @Valid @RequestBody List<AssetKeyDefinitionRequestDTO> requests) {
        log.info("REST request to batch-register {} catalog key definitions", requests.size());
        List<AssetKeyDefinitionResponseDTO> responses = service.registerKeysBatch(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PatchMapping("/keys/{assetKey}/status")
    public ResponseEntity<AssetKeyDefinitionResponseDTO> toggleKeyStatus(
            @PathVariable String assetKey,
            @RequestParam boolean enabled) {
        log.info("REST request to toggle status of catalog key: [{}] to enabled={}", assetKey, enabled);
        AssetKeyDefinitionResponseDTO response = service.toggleKeyStatus(assetKey, enabled);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/keys/{assetKey}")
    public ResponseEntity<Void> deleteKey(
            @PathVariable String assetKey) {
        log.info("REST request to delete catalog key definition: [{}]", assetKey);
        service.deleteKey(assetKey);
        return ResponseEntity.noContent().build();
    }
}