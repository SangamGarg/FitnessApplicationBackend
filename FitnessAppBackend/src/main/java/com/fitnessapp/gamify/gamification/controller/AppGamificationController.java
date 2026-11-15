package com.fitnessapp.gamify.gamification.controller;

import com.fitnessapp.gamify.gamification.model.dto.request.GameCompleteRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.request.GameStartRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.response.GameCatalogueResponseDto;
import com.fitnessapp.gamify.gamification.model.dto.response.GameDetailResponseDto;
import com.fitnessapp.gamify.gamification.model.dto.response.GameSessionResponseDto;
import com.fitnessapp.gamify.gamification.model.enums.GameType;
import com.fitnessapp.gamify.gamification.model.dto.response.GamificationHomeSummaryResponseDto;
import com.fitnessapp.gamify.gamification.model.dto.response.RewardHistoryResponseDto;
import com.fitnessapp.gamify.gamification.service.GamificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_GAMIFICATION_ENDPOINT;

@RestController
@RequestMapping(APP_GAMIFICATION_ENDPOINT)
@RequiredArgsConstructor
public class AppGamificationController {
    private final GamificationService service;


    @GetMapping("/home/summary")
    //Get XP, level, wallet coins and activity streak
    public ResponseEntity<GamificationHomeSummaryResponseDto> getGamificationHomeSummary() {
        return ResponseEntity.ok(service.getGamificationHomeSummary());
    }

    @GetMapping("/rewards/history")
    //Get paginated reward history
    public ResponseEntity<Page<RewardHistoryResponseDto>> history(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(service.getHistory(pageable));
    }


    @GetMapping("/games")
    public ResponseEntity<List<GameCatalogueResponseDto>> getActiveGames() {
        return ResponseEntity.ok(service.getActiveGames());
    }

    @GetMapping("/{gameType}/details")
    public ResponseEntity<GameDetailResponseDto> gameDetails(@PathVariable GameType gameType) {
        return ResponseEntity.ok(service.gameDetails(gameType));
    }

    @PostMapping("/sessions/start")
    public ResponseEntity<GameSessionResponseDto> start(@Valid @RequestBody GameStartRequestDto r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.start(r));
    }

    @PostMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<GameSessionResponseDto> complete(@PathVariable UUID sessionId, @Valid @RequestBody GameCompleteRequestDto r) {
        return ResponseEntity.ok(service.complete(sessionId, r));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<GameSessionResponseDto>> gameHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.gameHistory(PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Direction.DESC, "startedAt"))));
    }
}
