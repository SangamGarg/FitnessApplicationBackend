package com.fitnessapp.gamify.challenge.controller;

import com.fitnessapp.gamify.challenge.model.dto.ChallengeResponseDto;
import com.fitnessapp.gamify.challenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_CHALLENGE_ENDPOINT;

@RestController
@RequestMapping(APP_CHALLENGE_ENDPOINT)
@RequiredArgsConstructor
public class AppChallengeController {
    private final ChallengeService service;

    @GetMapping
    public ResponseEntity<List<ChallengeResponseDto>> available() {
        return ResponseEntity.ok(service.available());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ChallengeResponseDto> join(@PathVariable Long id) {
        return ResponseEntity.ok(service.join(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ChallengeResponseDto>> mine() {
        return ResponseEntity.ok(service.mine());
    }
}
