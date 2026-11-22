package com.fitnessapp.assets.controller;

import com.fitnessapp.assets.model.dto.request.AssetKeyDefinitionRequestDTO;
import com.fitnessapp.assets.model.dto.request.AssetMetadataUpdateRequestDTO;
import com.fitnessapp.assets.model.dto.request.AssetUploadRequestDTO;
import com.fitnessapp.assets.model.dto.response.AppMediaAssetResponseDTO;
import com.fitnessapp.assets.model.dto.response.AssetKeyDefinitionResponseDTO;
import com.fitnessapp.assets.model.enums.AssetCategory;
import com.fitnessapp.assets.service.PortalMediaAssetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_ASSET_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(PORTAL_DEV_ASSET_ENDPOINT)
@RequiredArgsConstructor
public class PortalMediaAssetController {

    private final PortalMediaAssetService portalService;

    //THE ENDPOINTS ARE SET IN ORDER OF EXECUTION

    @PostMapping("/keys")
    public ResponseEntity<AssetKeyDefinitionResponseDTO> registerKey(
            @Valid @RequestBody AssetKeyDefinitionRequestDTO request) {
        log.info("REST request to register a catalog key under category: [{}]", request.getCategory());
        AssetKeyDefinitionResponseDTO response = portalService.registerKey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/keys/batch")
    public ResponseEntity<List<AssetKeyDefinitionResponseDTO>> registerKeysBatch(
            @RequestBody @Valid @Size(min = 1, max = 100) List<@Valid AssetKeyDefinitionRequestDTO> requests) {
        log.info("REST request to batch-register {} catalog key definitions", requests.size());
        List<AssetKeyDefinitionResponseDTO> responses = portalService.registerKeysBatch(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppMediaAssetResponseDTO> uploadAsset(@Valid @ModelAttribute AssetUploadRequestDTO requestDTO, @RequestPart("file") MultipartFile file) {

        log.info("REST request to upload a new media asset");

        AppMediaAssetResponseDTO response = portalService.uploadNewAsset(requestDTO, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(value = "/{assetKey}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppMediaAssetResponseDTO> patchImage(@PathVariable String assetKey, @RequestPart("file") MultipartFile file) {

        log.info("REST request to replace an asset image");
        AppMediaAssetResponseDTO response = portalService.patchAssetImage(assetKey, file);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{assetKey}/metadata")
    public ResponseEntity<AppMediaAssetResponseDTO> updateMetadata(@PathVariable String assetKey, @RequestBody AssetMetadataUpdateRequestDTO requestDTO) {

        log.info("REST request to update asset metadata");
        AppMediaAssetResponseDTO response = portalService.updateAssetMetadata(assetKey, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{assetKey}/status")
    public ResponseEntity<AppMediaAssetResponseDTO> toggleStatus(
            @PathVariable String assetKey, @RequestParam boolean isActive) {

        log.info("REST request to toggle asset active status to [{}]", isActive);
        AppMediaAssetResponseDTO response = portalService.toggleAssetStatus(assetKey, isActive);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{assetKey}/image")
    public ResponseEntity<AppMediaAssetResponseDTO> deleteImage(@PathVariable String assetKey) {
        log.info("REST request to remove an asset image");
        return ResponseEntity.ok(portalService.deleteAssetImage(assetKey));
    }



    /*
     * Catalog
     * */


    @GetMapping("/categories")
    public ResponseEntity<List<AssetCategory>> getCategories() {
        log.info("REST request to fetch all asset categories for catalog dropdown");
        List<AssetCategory> categories = portalService.getCategories();
        return ResponseEntity.ok(categories);
    }


    @GetMapping("/keys")
    public ResponseEntity<List<AssetKeyDefinitionResponseDTO>> getKeys(
            @RequestParam(required = false) AssetCategory category) {
        log.info("REST request to fetch catalog keys filtered by category: [{}]", category != null ? category : "ALL");
        List<AssetKeyDefinitionResponseDTO> keys = portalService.getKeys(category);
        return ResponseEntity.ok(keys);
    }



    @PatchMapping("/keys/{assetKey}/status")
    public ResponseEntity<AssetKeyDefinitionResponseDTO> toggleKeyStatus(
            @PathVariable String assetKey,
            @RequestParam boolean enabled) {
        log.info("REST request to toggle catalog key status to enabled={}", enabled);
        AssetKeyDefinitionResponseDTO response = portalService.toggleKeyStatus(assetKey, enabled);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/keys/{assetKey}")
    public ResponseEntity<Void> deleteKey(
            @PathVariable String assetKey) {
        log.info("REST request to delete a catalog key definition");
        portalService.deleteKey(assetKey);
        return ResponseEntity.noContent().build();
    }

}