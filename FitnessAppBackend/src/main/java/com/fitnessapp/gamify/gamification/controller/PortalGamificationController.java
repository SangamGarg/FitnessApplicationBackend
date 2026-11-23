package com.fitnessapp.gamify.gamification.controller;

import com.fitnessapp.gamify.gamification.model.dto.request.CreateGameRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.request.PatchGameRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.response.GameCatalogueResponseDto;
import com.fitnessapp.gamify.gamification.model.dto.response.PortalGameResponseDto;
import com.fitnessapp.gamify.gamification.model.enums.GameType;
import com.fitnessapp.gamify.gamification.service.GamificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_GAMIFICATION_ENDPOINT;

@RestController
@RequestMapping(PORTAL_DEV_GAMIFICATION_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
public class PortalGamificationController {
    private final GamificationService service;

    @GetMapping("/games")
    public ResponseEntity<List<PortalGameResponseDto>> getGamesForPortal() {
        return ResponseEntity.ok(service.getGamesForPortal());
    }

    @PostMapping(value = "/games", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GameCatalogueResponseDto> createGame(
            @RequestPart("request") @Valid CreateGameRequestDto request,
            @RequestPart("image") MultipartFile image,
            @RequestPart("gif") MultipartFile gif,
            @RequestPart("video") MultipartFile video) {
        log.info("Received request to create game: {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createGame(request, image, gif, video));
    }

    @PatchMapping(value = "/games/{gameType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PortalGameResponseDto> patchGame(
            @PathVariable GameType gameType,
            @RequestPart("request") @Valid PatchGameRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "gif", required = false) MultipartFile gif,
            @RequestPart(value = "video", required = false) MultipartFile video) {
        log.info("Received request to patch game: {}", gameType);
        return ResponseEntity.ok(service.patchGame(gameType, request, image, gif, video));
    }

    @PatchMapping("/games/{gameType}/status")
    public ResponseEntity<PortalGameResponseDto> updateGameStatus(
            @PathVariable GameType gameType,
            @RequestParam boolean active) {
        log.info("Received request to update game status: {} active={}", gameType, active);
        return ResponseEntity.ok(service.updateGameStatus(gameType, active));
    }

    @DeleteMapping("/games/{gameType}")
    public ResponseEntity<Void> deleteGame(@PathVariable GameType gameType) {
        log.info("Received request to delete game: {}", gameType);
        service.deleteGame(gameType);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/games/{gameType}/deactivate")
    public ResponseEntity<Void> deactivateGame(@PathVariable GameType gameType) {
        log.info("Received request to deactivate game: {}", gameType);
        service.deactivateGame(gameType);
        return ResponseEntity.noContent().build();
    }

}
