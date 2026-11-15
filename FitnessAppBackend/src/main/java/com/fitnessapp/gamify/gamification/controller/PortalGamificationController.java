package com.fitnessapp.gamify.gamification.controller;

import com.fitnessapp.gamify.gamification.model.dto.request.CreateGameRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.response.GameCatalogueResponseDto;
import com.fitnessapp.gamify.gamification.model.enums.GameType;
import com.fitnessapp.gamify.gamification.service.GamificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_GAMIFICATION_ENDPOINT;

@RestController
@RequestMapping(PORTAL_GAMIFICATION_ENDPOINT)
@RequiredArgsConstructor
public class PortalGamificationController {
    private final GamificationService service;

    @PostMapping("/games")
    public ResponseEntity<GameCatalogueResponseDto> createGame(@RequestBody @Valid CreateGameRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createGame(request));
    }

    @PatchMapping("/games/{gameType}/deactivate")
    public ResponseEntity<Void> deactivateGame(@PathVariable GameType gameType) {
        service.deactivateGame(gameType);
        return ResponseEntity.noContent().build();
    }

}
