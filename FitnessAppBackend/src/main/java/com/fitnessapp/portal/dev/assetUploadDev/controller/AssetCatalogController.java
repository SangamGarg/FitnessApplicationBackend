package com.fitnessapp.portal.dev.assetUploadDev.controller;

import com.fitnessapp.portal.dev.assetUploadDev.service.AssetCatalogService;
import com.fitnessapp.portal.dev.assetUploadDev.model.*;
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

import static com.fitnessapp.portal.dev.shared.DevUtils.API_PREFIX_ASSETS;

/**
 * Controller for managing the catalog of asset categories and registered key slots.
 */
@Slf4j
@RestController
@RequestMapping(API_PREFIX_ASSETS + "/catalog")
@RequiredArgsConstructor
@Tag(name = "Asset Catalog", description = "Endpoints for managing asset categories and key definition slots")
public class AssetCatalogController {

    private final AssetCatalogService service;

    /**
     * Categories for the first dropdown.
     */
    @Operation(
            summary = "Get all asset categories",
            description = "Retrieves the complete list of available asset categories used to group media assets."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AssetCategory.class)))
            )
    })
    @GetMapping("/categories")
    public ResponseEntity<List<AssetCategory>> getCategories() {
        log.info("REST request to fetch all asset categories for catalog dropdown");
        List<AssetCategory> categories = service.getCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Registered asset keys for the selected category or all if category is omitted.
     */
    @Operation(
            summary = "Get registered asset key definitions",
            description = "Fetches active key definition slots. Optionally filtered by category; returns all if omitted."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Key definitions retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AssetKeyDefinitionResponseDTO.class)))
            )
    })
    @GetMapping("/keys")
    public ResponseEntity<List<AssetKeyDefinitionResponseDTO>> getKeys(
            @Parameter(description = "Optional category filter to restrict key definitions", example = "APP_BRANDING")
            @RequestParam(required = false) AssetCategory category) {
        log.info("REST request to fetch catalog keys filtered by category: [{}]", category != null ? category : "ALL");
        List<AssetKeyDefinitionResponseDTO> keys = service.getKeys(category);
        return ResponseEntity.ok(keys);
    }

    /**
     * Register a single slot once; developers can then select it from upload dropdowns.
     */
    @Operation(
            summary = "Register a new asset key slot",
            description = "Registers a new machine-readable asset key definition slot (e.g., HOME_HERO_BANNER) for later asset uploads."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Key definition registered successfully",
                    content = @Content(schema = @Schema(implementation = AssetKeyDefinitionResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid key format or key already exists", content = @Content)
    })
    @PostMapping("/keys")
    public ResponseEntity<AssetKeyDefinitionResponseDTO> registerKey(
            @Parameter(description = "Asset key definition details", required = true)
            @Valid @RequestBody AssetKeyDefinitionRequestDTO request) {
        log.info("REST request to register new catalog key: [{}] under category: [{}]",
                request.getAssetKey(), request.getCategory());
        AssetKeyDefinitionResponseDTO response = service.registerKey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Batch register multiple asset key definitions at once.
     */
    @Operation(
            summary = "Batch register asset key slots",
            description = "Registers multiple asset key definitions simultaneously in a single atomic transaction."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "All keys registered successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AssetKeyDefinitionResponseDTO.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Duplicate key detected in payload or database", content = @Content)
    })
    @PostMapping("/keys/batch")
    public ResponseEntity<List<AssetKeyDefinitionResponseDTO>> registerKeysBatch(
            @Parameter(description = "List of asset key definitions to register", required = true)
            @Valid @RequestBody List<AssetKeyDefinitionRequestDTO> requests) {
        log.info("REST request to batch-register {} catalog key definitions", requests.size());
        List<AssetKeyDefinitionResponseDTO> responses = service.registerKeysBatch(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * Toggle the enabled/disabled status of an asset key definition (soft delete / reactivate).
     */
    @Operation(
            summary = "Toggle asset key status",
            description = "Enables or disables an asset key slot. Disabling hides it from standard selection without hard deleting."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Key status updated successfully",
                    content = @Content(schema = @Schema(implementation = AssetKeyDefinitionResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Asset key definition not found", content = @Content)
    })
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

    /**
     * Permanently delete a catalog key definition if no media files are bound to it.
     */
    @Operation(
            summary = "Delete an asset key definition",
            description = "Permanently deletes an unused key definition slot. Fails if an active uploaded asset is referencing it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Key definition deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Asset key definition not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Cannot delete: active media asset is linked to this key", content = @Content)
    })
    @DeleteMapping("/keys/{assetKey}")
    public ResponseEntity<Void> deleteKey(
            @Parameter(description = "The unique asset key identifier to permanently remove", required = true, example = "OLD_PROMO_BANNER")
            @PathVariable String assetKey) {
        log.info("REST request to delete catalog key definition: [{}]", assetKey);
        service.deleteKey(assetKey);
        return ResponseEntity.noContent().build();
    }
}