package com.fitnessapp.portal.dev.assetUploadDev.controller;

import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetResponseDTO;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetMetadataUpdateRequestDTO;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetUploadRequestDTO;
import com.fitnessapp.portal.dev.assetUploadDev.service.PortalMediaAssetService;
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

import static com.fitnessapp.portal.dev.shared.DevUtils.API_PREFIX_ASSETS;

/**
 * Administrative REST Controller for managing application media assets.
 * <p>
 * Exposes endpoints for dev/portal users to upload new media assets,
 * patch existing binary files, update textual metadata, and soft-delete/toggle
 * asset visibility status across client applications.
 */
@Slf4j
@RestController
@RequestMapping(API_PREFIX_ASSETS)
@RequiredArgsConstructor
@Tag(name = "Image And About", description = "Administrative endpoints for managing application media assets and illustrations")
public class PortalMediaAssetController {

    private final PortalMediaAssetService portalService;

    /**
     * Uploads a new media asset along with its metadata.
     *
     * @param requestDTO Metadata fields including unique assetKey, category, title, alt text, and description.
     * @param file       The binary image/GIF file to be stored.
     * @return The persisted media asset details with HTTP 201 (Created).
     */
    @Operation(
            summary = "Upload a new media asset",
            description = "Stores the image file to cloud storage and records its metadata with a unique assetKey."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asset uploaded successfully",
                    content = @Content(schema = @Schema(implementation = AppMediaAssetResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or duplicate assetKey", content = @Content),
            @ApiResponse(responseCode = "500", description = "Storage upload failure", content = @Content)
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppMediaAssetResponseDTO> uploadAsset(
            @Parameter(description = "Asset metadata fields", required = true)
            @Valid @ModelAttribute AssetUploadRequestDTO requestDTO,

            @Parameter(description = "Binary image file (JPEG, PNG, WebP, GIF)", required = true)
            @RequestPart("file") MultipartFile file) {

        log.info("REST request to upload new media asset with key: [{}]", requestDTO.getAssetKey());

        // Delegate file persistence and entity creation to the service layer
        AppMediaAssetResponseDTO response = portalService.uploadNewAsset(requestDTO, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Replaces the binary file of an existing media asset without altering its textual metadata.
     *
     * @param assetKey Unique machine-readable identifier of the asset.
     * @param file     The new image file replacement.
     * @return The updated media asset details with HTTP 200 (OK).
     */
    @Operation(
            summary = "Patch asset image file",
            description = "Uploads a new binary file, replaces the existing storage URL, and removes the stale file."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset image replaced successfully",
                    content = @Content(schema = @Schema(implementation = AppMediaAssetResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Asset key not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Storage upload failure", content = @Content)
    })
    @PatchMapping(value = "/{assetKey}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppMediaAssetResponseDTO> patchImage(
            @Parameter(description = "Unique asset key to identify the record", required = true, example = "ERR_SERVER_DOWN")
            @PathVariable String assetKey,

            @Parameter(description = "Replacement image binary file", required = true)
            @RequestPart("file") MultipartFile file) {

        log.info("REST request to replace image file for assetKey: [{}]", assetKey);

        // Upload new file, update image URL reference, and purge obsolete image from storage
        AppMediaAssetResponseDTO response = portalService.patchAssetImage(assetKey, file);

        return ResponseEntity.ok(response);
    }

    /**
     * Updates textual properties (title, altText, description, category) of an existing asset.
     *
     * @param assetKey   Unique machine-readable identifier of the asset.
     * @param requestDTO Object containing fields to update.
     * @return The updated media asset details with HTTP 200 (OK).
     */
    @Operation(
            summary = "Update asset metadata",
            description = "Modifies non-binary attributes such as title, category, alt text, or description."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metadata updated successfully",
                    content = @Content(schema = @Schema(implementation = AppMediaAssetResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Asset key not found", content = @Content)
    })
    @PatchMapping("/{assetKey}/metadata")
    public ResponseEntity<AppMediaAssetResponseDTO> updateMetadata(
            @Parameter(description = "Unique asset key to identify the record", required = true, example = "ABOUT_US_HERO")
            @PathVariable String assetKey,

            @Parameter(description = "Partial metadata attributes to update", required = true)
            @RequestBody AssetMetadataUpdateRequestDTO requestDTO) {

        log.info("REST request to update metadata for assetKey: [{}]", assetKey);

        // Apply partial field updates while preserving unchanged values
        AppMediaAssetResponseDTO response = portalService.updateAssetMetadata(assetKey, requestDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * Toggles the active/visibility status of an asset (soft delete / soft enable).
     *
     * @param assetKey Unique machine-readable identifier of the asset.
     * @param isActive Boolean flag: true to make available to clients, false to hide.
     * @return The updated media asset details with HTTP 200 (OK).
     */
    @Operation(
            summary = "Toggle asset active status",
            description = "Enables or disables an asset so clients will or will not receive it in queries without hard deletion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = AppMediaAssetResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Asset key not found", content = @Content)
    })
    @PatchMapping("/{assetKey}/status")
    public ResponseEntity<AppMediaAssetResponseDTO> toggleStatus(
            @Parameter(description = "Unique asset key to identify the record", required = true, example = "SYSTEM_UNDER_MAINTENANCE")
            @PathVariable String assetKey,

            @Parameter(description = "Flag specifying target active state", required = true, example = "false")
            @RequestParam boolean isActive) {

        log.info("REST request to toggle active status for assetKey: [{}] to [{}]", assetKey, isActive);

        // Update active flag in repository
        AppMediaAssetResponseDTO response = portalService.toggleAssetStatus(assetKey, isActive);

        return ResponseEntity.ok(response);
    }
}