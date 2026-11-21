package com.fitnessapp.assets.controller;

import com.fitnessapp.assets.model.dto.response.AppMediaAssetResponseDTO;
import com.fitnessapp.assets.model.dto.request.AssetMetadataUpdateRequestDTO;
import com.fitnessapp.assets.model.dto.request.AssetUploadRequestDTO;
import com.fitnessapp.assets.service.PortalMediaAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_ASSET_ENDPOINT;


@Slf4j
@RestController
@RequestMapping(PORTAL_DEV_ASSET_ENDPOINT)
@RequiredArgsConstructor
public class PortalMediaAssetController {

    private final PortalMediaAssetService portalService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppMediaAssetResponseDTO> uploadAsset(@Valid @ModelAttribute AssetUploadRequestDTO requestDTO, @RequestPart("file") MultipartFile file) {

        log.info("REST request to upload new media asset with key: [{}]", requestDTO.getAssetKey());

        AppMediaAssetResponseDTO response = portalService.uploadNewAsset(requestDTO, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(value = "/{assetKey}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppMediaAssetResponseDTO> patchImage(@PathVariable String assetKey, @RequestPart("file") MultipartFile file) {

        log.info("REST request to replace image file for assetKey: [{}]", assetKey);
        AppMediaAssetResponseDTO response = portalService.patchAssetImage(assetKey, file);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{assetKey}/metadata")
    public ResponseEntity<AppMediaAssetResponseDTO> updateMetadata(@PathVariable String assetKey, @RequestBody AssetMetadataUpdateRequestDTO requestDTO) {

        log.info("REST request to update metadata for assetKey: [{}]", assetKey);
        AppMediaAssetResponseDTO response = portalService.updateAssetMetadata(assetKey, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{assetKey}/status")
    public ResponseEntity<AppMediaAssetResponseDTO> toggleStatus(
            @PathVariable String assetKey, @RequestParam boolean isActive) {

        log.info("REST request to toggle active status for assetKey: [{}] to [{}]", assetKey, isActive);
        AppMediaAssetResponseDTO response = portalService.toggleAssetStatus(assetKey, isActive);
        return ResponseEntity.ok(response);
    }
}