package com.fitnessapp.gamification.achievement;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_ACHIEVEMENT_ENDPOINT;

@RestController
@RequestMapping(APP_ACHIEVEMENT_ENDPOINT)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AchievementController {
    private final AchievementService service;

    @GetMapping
    public ResponseEntity<List<AchievementResponseDto>> list(Authentication a) {
        return ResponseEntity.ok(service.list(a.getName()));
    }
}
