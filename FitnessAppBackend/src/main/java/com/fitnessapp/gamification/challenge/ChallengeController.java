package com.fitnessapp.gamification.challenge;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_CHALLENGE_ENDPOINT;

@RestController
@RequestMapping(APP_CHALLENGE_ENDPOINT)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Challenges")
public class ChallengeController {
    private final ChallengeService service;

    @GetMapping
    public ResponseEntity<List<ChallengeResponseDto>> available(Authentication a) {
        return ResponseEntity.ok(service.available(a.getName()));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ChallengeResponseDto> join(Authentication a, @PathVariable Long id) {
        return ResponseEntity.ok(service.join(a.getName(), id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ChallengeResponseDto>> mine(Authentication a) {
        return ResponseEntity.ok(service.mine(a.getName()));
    }
}
