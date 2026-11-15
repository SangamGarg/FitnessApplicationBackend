package com.fitnessapp.gamify.achievement.controller;

import com.fitnessapp.gamify.achievement.model.AchievementResponseDto;
import com.fitnessapp.gamify.achievement.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_ACHIEVEMENT_ENDPOINT;

@RestController
@RequestMapping(APP_ACHIEVEMENT_ENDPOINT)
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService service;

    @GetMapping
    public ResponseEntity<List<AchievementResponseDto>> list() {
        return ResponseEntity.ok(service.list());
    }
}
