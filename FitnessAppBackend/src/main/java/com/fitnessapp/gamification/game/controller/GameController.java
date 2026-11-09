package com.fitnessapp.gamification.game.controller;

import com.fitnessapp.gamification.game.service.GameService;
import com.fitnessapp.gamification.game.model.dto.GameCompleteRequestDto;
import com.fitnessapp.gamification.game.model.dto.GameSessionResponseDto;
import com.fitnessapp.gamification.game.model.dto.GameStartRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_GAME_ENDPOINT;

@RestController
@RequestMapping(APP_GAME_ENDPOINT)
@RequiredArgsConstructor
public class GameController {
    private final GameService service;

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> catalogue() {
        return ResponseEntity.ok(service.catalogue());
    }

    @PostMapping("/sessions")
    public ResponseEntity<GameSessionResponseDto> start(@Valid @RequestBody GameStartRequestDto r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.start(r));
    }

    @PostMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<GameSessionResponseDto> complete( @PathVariable UUID sessionId, @Valid @RequestBody GameCompleteRequestDto r) {
        return ResponseEntity.ok(service.complete( sessionId, r));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<GameSessionResponseDto>> history( @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.history(PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Direction.DESC, "startedAt"))));
    }
}
