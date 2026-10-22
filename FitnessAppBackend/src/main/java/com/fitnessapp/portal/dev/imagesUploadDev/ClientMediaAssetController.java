package com.fitnessapp.portal.dev.imagesUploadDev;

import com.fitnessapp.portal.dev.imagesUploadDev.AssetCategory;
import com.fitnessapp.portal.dev.imagesUploadDev.models.AppMediaAssetResponseDTO;
import com.fitnessapp.portal.dev.imagesUploadDev.ClientMediaAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/app/assets")
@RequiredArgsConstructor
public class ClientMediaAssetController {

    private final ClientMediaAssetService clientService;

    @GetMapping("/{assetKey}")
    public ResponseEntity<AppMediaAssetResponseDTO> getAssetByKey(@PathVariable String assetKey) {
        log.info("Client request for assetKey: [{}]", assetKey);
        return ResponseEntity.ok(clientService.getActiveAssetByKey(assetKey));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<AppMediaAssetResponseDTO>> getAssetsByCategory(@PathVariable AssetCategory category) {
        log.info("Client request for assets under category: [{}]", category);
        return ResponseEntity.ok(clientService.getActiveAssetsByCategory(category));
    }

    @GetMapping
    public ResponseEntity<List<AppMediaAssetResponseDTO>> getAllActiveAssets() {
        log.info("Client request to load all active assets");
        return ResponseEntity.ok(clientService.getAllActiveAssets());
    }
}